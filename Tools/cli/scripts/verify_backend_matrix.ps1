param(
  [string[]] $Targets = @(),
  [switch] $SkipDbReset,
  [int] $StartupTimeoutSeconds = 240
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$env:PYTHONPATH = Join-Path $RepoRoot "Tools\cli\src"

$AllTargets = @(
  @{ Name = "post-java-springboot-maven-postgresql"; Domain = "post"; Db = "postgresql"; Port = 8011 },
  @{ Name = "post-typescript-nestjs-npm-postgresql"; Domain = "post"; Db = "postgresql"; Port = 8012 },
  @{ Name = "shop-java-springboot-maven-postgresql"; Domain = "shop"; Db = "postgresql"; Port = 8021 },
  @{ Name = "shop-typescript-nestjs-npm-postgresql"; Domain = "shop"; Db = "postgresql"; Port = 8022 },
  @{ Name = "post-java-springboot-maven-mysql"; Domain = "post"; Db = "mysql"; Port = 8013 },
  @{ Name = "post-typescript-nestjs-npm-mysql"; Domain = "post"; Db = "mysql"; Port = 8014 },
  @{ Name = "shop-java-springboot-maven-mysql"; Domain = "shop"; Db = "mysql"; Port = 8023 },
  @{ Name = "shop-typescript-nestjs-npm-mysql"; Domain = "shop"; Db = "mysql"; Port = 8024 },
  @{ Name = "post-java-springboot-gradle-postgresql"; Domain = "post"; Db = "postgresql"; Port = 8015 },
  @{ Name = "post-java-springboot-gradle-mysql"; Domain = "post"; Db = "mysql"; Port = 8016 },
  @{ Name = "shop-java-springboot-gradle-postgresql"; Domain = "shop"; Db = "postgresql"; Port = 8025 },
  @{ Name = "shop-java-springboot-gradle-mysql"; Domain = "shop"; Db = "mysql"; Port = 8026 }
)

if ($Targets.Count -gt 0) {
  $AllTargets = $AllTargets | Where-Object { $Targets -contains $_.Name }
}

function Write-Step([string] $Message) {
  Write-Host "[$(Get-Date -Format HH:mm:ss)] $Message"
}

function Invoke-Pb([string[]] $PbArgs) {
  Push-Location $RepoRoot
  try {
    & python -m pbcli.main @PbArgs
    if ($LASTEXITCODE -ne 0) {
      throw "pb $($PbArgs -join ' ') failed with exit code $LASTEXITCODE"
    }
  } finally {
    Pop-Location
  }
}

function Invoke-HttpJson(
  [string] $Method,
  [string] $BaseUrl,
  [string] $Path,
  [object] $Body = $null,
  [string] $Token = ""
) {
  $headers = @{}
  if ($Token) {
    $headers["Authorization"] = "Bearer $Token"
  }

  $params = @{
    Method = $Method
    Uri = "$BaseUrl$Path"
    Headers = $headers
  }
  if ($null -ne $Body) {
    $params["ContentType"] = "application/json"
    $params["Body"] = ($Body | ConvertTo-Json -Depth 20)
  }

  try {
    return Invoke-RestMethod @params
  } catch {
    $message = $_.Exception.Message
    if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
      $message = "$message`n$($_.ErrorDetails.Message)"
    }
    throw "$Method $Path failed: $message"
  }
}

function Assert-Envelope([object] $Response, [string] $Label) {
  if ($null -eq $Response -or $Response.success -ne $true) {
    throw "$Label did not return success envelope"
  }
}

function Wait-Backend([string] $BaseUrl) {
  $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    try {
      $health = Invoke-HttpJson "GET" $BaseUrl "/api/v1/health"
      Assert-Envelope $health "health"
      return
    } catch {
      Start-Sleep -Seconds 3
    }
  }
  throw "Backend did not become healthy within $StartupTimeoutSeconds seconds: $BaseUrl"
}

function Test-Docs([string] $BaseUrl) {
  try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/docs" -UseBasicParsing -MaximumRedirection 5
    if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 400) {
      throw "unexpected status $($response.StatusCode)"
    }
  } catch {
    throw "GET /docs failed: $($_.Exception.Message)"
  }
}

function Login-User([string] $BaseUrl, [string] $Domain) {
  $email = if ($Domain -eq "post") { "post-user-1@example.com" } else { "shop-user-1@example.com" }
  $login = Invoke-HttpJson "POST" $BaseUrl "/api/v1/auth/login" @{ email = $email; password = "Password1!" }
  Assert-Envelope $login "user login"
  return $login.data
}

function Login-Admin([string] $BaseUrl, [string] $Domain) {
  $email = if ($Domain -eq "post") { "admin-post-1@example.com" } else { "admin-shop-1@example.com" }
  $login = Invoke-HttpJson "POST" $BaseUrl "/api/v1/admin/auth/login" @{ email = $email; password = "AdminPassword1!" }
  Assert-Envelope $login "admin login"
  return $login.data
}

function Test-CommonFlow([string] $BaseUrl, [string] $Domain) {
  $user = Login-User $BaseUrl $Domain
  $me = Invoke-HttpJson "GET" $BaseUrl "/api/v1/users/me" $null $user.accessToken
  Assert-Envelope $me "users me"

  $refresh = Invoke-HttpJson "POST" $BaseUrl "/api/v1/auth/refresh" @{ refreshToken = $user.refreshToken }
  Assert-Envelope $refresh "user refresh"

  $logout = Invoke-HttpJson "POST" $BaseUrl "/api/v1/auth/logout" $null $user.accessToken
  Assert-Envelope $logout "user logout"

  $admin = Login-Admin $BaseUrl $Domain
  $adminMe = Invoke-HttpJson "GET" $BaseUrl "/api/v1/admin/me" $null $admin.accessToken
  Assert-Envelope $adminMe "admin me"

  $adminRefresh = Invoke-HttpJson "POST" $BaseUrl "/api/v1/admin/auth/refresh" @{ refreshToken = $admin.refreshToken }
  Assert-Envelope $adminRefresh "admin refresh"

  $adminLogout = Invoke-HttpJson "POST" $BaseUrl "/api/v1/admin/auth/logout" $null $admin.accessToken
  Assert-Envelope $adminLogout "admin logout"
}

function Test-PostFlow([string] $BaseUrl) {
  $user = Login-User $BaseUrl "post"
  $admin = Login-Admin $BaseUrl "post"

  $boards = Invoke-HttpJson "GET" $BaseUrl "/api/v1/boards"
  Assert-Envelope $boards "boards"
  $boardId = [int] $boards.data[0].id

  $board = Invoke-HttpJson "GET" $BaseUrl "/api/v1/boards/$boardId"
  Assert-Envelope $board "board detail"

  $posts = Invoke-HttpJson "GET" $BaseUrl "/api/v1/posts?page=1&limit=10"
  Assert-Envelope $posts "posts"
  if ($null -eq $posts.meta) {
    throw "posts list is missing meta"
  }

  $firstPostId = [int] $posts.data[0].id
  $postDetail = Invoke-HttpJson "GET" $BaseUrl "/api/v1/posts/$firstPostId"
  Assert-Envelope $postDetail "post detail"
  foreach ($field in @("viewCount", "likeCount", "commentCount")) {
    if (-not ($postDetail.data.PSObject.Properties.Name -contains $field)) {
      throw "post detail missing $field"
    }
  }

  $suffix = Get-Random
  $createdPost = Invoke-HttpJson "POST" $BaseUrl "/api/v1/posts" @{
    boardId = $boardId
    title = "HTTP matrix post $suffix"
    content = "HTTP matrix content"
  } $user.accessToken
  Assert-Envelope $createdPost "post create"
  $postId = [int] $createdPost.data.id

  $comment = Invoke-HttpJson "POST" $BaseUrl "/api/v1/posts/$postId/comments" @{
    content = "HTTP matrix comment"
  } $user.accessToken
  Assert-Envelope $comment "comment create"

  $like = Invoke-HttpJson "POST" $BaseUrl "/api/v1/posts/$postId/likes" $null $user.accessToken
  Assert-Envelope $like "like create"

  $adminPosts = Invoke-HttpJson "GET" $BaseUrl "/api/v1/admin/posts?page=1&limit=10" $null $admin.accessToken
  Assert-Envelope $adminPosts "admin posts"

  $status = Invoke-HttpJson "PATCH" $BaseUrl "/api/v1/admin/posts/$postId/status" @{
    status = "HIDDEN"
  } $admin.accessToken
  Assert-Envelope $status "admin post status"
}

function Test-ShopFlow([string] $BaseUrl) {
  $user = Login-User $BaseUrl "shop"
  $admin = Login-Admin $BaseUrl "shop"

  $categories = Invoke-HttpJson "GET" $BaseUrl "/api/v1/categories"
  Assert-Envelope $categories "categories"

  $products = Invoke-HttpJson "GET" $BaseUrl "/api/v1/products?page=1&limit=10"
  Assert-Envelope $products "products"
  $productId = [int] $products.data[0].id

  $product = Invoke-HttpJson "GET" $BaseUrl "/api/v1/products/$productId"
  Assert-Envelope $product "product detail"
  $optionId = $null
  if ($product.data.options -and $product.data.options.Count -gt 0) {
    $optionId = [int] $product.data.options[0].id
  }

  $address = Invoke-HttpJson "POST" $BaseUrl "/api/v1/addresses" @{
    recipientName = "HTTP Matrix"
    phone = "01012345678"
    zipCode = "12345"
    address1 = "Seoul matrix road"
    address2 = "1001"
    isDefault = $true
  } $user.accessToken
  Assert-Envelope $address "address create"
  $addressId = [int] $address.data.id

  $cartPayload = @{
    productId = $productId
    quantity = 1
  }
  if ($null -ne $optionId) {
    $cartPayload.productOptionId = $optionId
  }
  $cart = Invoke-HttpJson "POST" $BaseUrl "/api/v1/cart-items" $cartPayload $user.accessToken
  Assert-Envelope $cart "cart create"
  $cartItemId = [int] $cart.data.id

  $order = Invoke-HttpJson "POST" $BaseUrl "/api/v1/orders" @{
    cartItemIds = @($cartItemId)
    addressId = $addressId
  } $user.accessToken
  Assert-Envelope $order "order create"
  $orderId = [int] $order.data.order.id
  if (-not $order.data.orderAddress -or -not $order.data.orderItems) {
    throw "order create response missing orderAddress/orderItems"
  }

  $orderDetail = Invoke-HttpJson "GET" $BaseUrl "/api/v1/orders/$orderId" $null $user.accessToken
  Assert-Envelope $orderDetail "order detail"
  foreach ($field in @("order", "orderAddress", "orderItems", "payment")) {
    if (-not ($orderDetail.data.PSObject.Properties.Name -contains $field)) {
      throw "order detail missing $field"
    }
  }

  $payment = Invoke-HttpJson "POST" $BaseUrl "/api/v1/payments" @{
    orderId = $orderId
    paymentMethod = "mock"
  } $user.accessToken
  Assert-Envelope $payment "payment create"

  $paidOrder = Invoke-HttpJson "GET" $BaseUrl "/api/v1/orders/$orderId" $null $user.accessToken
  Assert-Envelope $paidOrder "paid order detail"
  $orderItemId = [int] $paidOrder.data.orderItems[0].id

  $review = Invoke-HttpJson "POST" $BaseUrl "/api/v1/order-items/$orderItemId/reviews" @{
    rating = 5
    content = "HTTP matrix review"
  } $user.accessToken
  Assert-Envelope $review "review create"

  $adminOrders = Invoke-HttpJson "GET" $BaseUrl "/api/v1/admin/orders?page=1&limit=10" $null $admin.accessToken
  Assert-Envelope $adminOrders "admin orders"

  $orderStatus = Invoke-HttpJson "PATCH" $BaseUrl "/api/v1/admin/orders/$orderId/status" @{
    orderStatus = "PREPARING"
  } $admin.accessToken
  Assert-Envelope $orderStatus "admin order status"

  $adminReviews = Invoke-HttpJson "GET" $BaseUrl "/api/v1/admin/reviews?page=1&limit=10" $null $admin.accessToken
  Assert-Envelope $adminReviews "admin reviews"
}

function Test-Target([hashtable] $Target) {
  $baseUrl = "http://localhost:$($Target.Port)"
  Write-Step "Resetting $($Target.Db)/$($Target.Domain)"
  if (-not $SkipDbReset) {
    Invoke-Pb @("db", "reset", $Target.Db, $Target.Domain)
  }

  Write-Step "Starting $($Target.Name) on $($Target.Port)"
  Invoke-Pb @("up", $Target.Name, "--port", [string] $Target.Port)

  try {
    Wait-Backend $baseUrl
    Test-Docs $baseUrl
    Test-CommonFlow $baseUrl $Target.Domain
    if ($Target.Domain -eq "post") {
      Test-PostFlow $baseUrl
    } else {
      Test-ShopFlow $baseUrl
    }
    Write-Step "PASS $($Target.Name)"
  } finally {
    Write-Step "Stopping $($Target.Name)"
    try {
      Invoke-Pb @("down", $Target.Name)
    } catch {
      Write-Warning "Failed to stop $($Target.Name): $($_.Exception.Message)"
    }
  }
}

Write-Step "Starting database services"
Invoke-Pb @("db", "up")

$failures = @()
foreach ($target in $AllTargets) {
  try {
    Test-Target $target
  } catch {
    $failures += "$($target.Name): $($_.Exception.Message)"
    Write-Host "ERROR: $($failures[-1])" -ForegroundColor Red
  }
}

if ($failures.Count -gt 0) {
  Write-Host ""
  Write-Host "Backend matrix failed:"
  $failures | ForEach-Object { Write-Host "- $_" }
  exit 1
}

Write-Step "All backend matrix checks passed."
