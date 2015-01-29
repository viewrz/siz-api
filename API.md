API Specs
=========

# Create a token
```
POST /tokens

{}
```

## Results
```
{
   "tokens" : {
      "href" : "/tokens/J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje",
      "id" : "J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje"
   }
}
```


# Create an user
## Request
```
POST /users
X-Access-Token: J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje

{
  "users": {
    "email" : "paul@example.com",
    "password" : "6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e",
    "username" : "paul.the.boss"
  }
}
```
## Notes
- password is a sha256(password+salt)
- password is not stored the same way as it's received :)

## Results
```
HTTP/1.1 201 Created
Content-Type: application/vnd.api+json

{
  "users": {
     "id":"548bf7d0e3bfc67d4d7c2cb6",
     "email":"paul@example.com",
     "username" : "paul.the.boss",
     "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
  },
  "tokens": {
     "user" : {
        "id" : "548bf7d0e3bfc67d4d7c2cb6",
        "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
     },
     "id":"fuEyvqImw2xbywewZAUHkFMo8xJO7eSOAOjkaRRSTTfzRTqdblN65Mx7O2JhmzVc"
  }
}
```
```
HTTP/1.1 409 Conflict
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"An user with email paul@example.com already exists"
   }
}
```

```
HTTP/1.1 400 Bad Request
Content-Type: application/vnd.api+json
{
    "errors" : [
        {"title":"error with field /email : invalid email address"},
        {"title":"error with field /username : should be from 2 to 20 characters and contains only 0-9a-zA-Z."},
        {"title":"error with field /password : invalid sha256"}
    ]
}
```

```
HTTP/1.1 401 Unauthorized
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"Unknown token J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje"
   }
}
```

```
HTTP/1.1 400 Bad Request
Content-Type: application/vnd.api+json

{
   "errors" : {
      "title" : "An user is already logged on this token, discard this token and create a new one."
   }
}

```

# Create an user with facebook
## Request
```
POST /users
X-Access-Token: J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje

{
  "users": {
    "email" : "paul@example.com",
    "facebookToken" : "CAADOJKDKJLDJMLDSKdsklokfdmlgdfpo878UNKJnkjnNKJNKJNIUG6T56789jtyR5YU7Hyuitusijfddslofj6789ihjnkjTYChg56789NJUhbvgtcyr67yuvgft",
    "username" : "paul.the.boss"
  }
}
```
## Notes
- email and username are optional

## Results
```
HTTP/1.1 201 Created
Content-Type: application/vnd.api+json

{
  "users": {
     "id":"548bf7d0e3bfc67d4d7c2cb6",
     "email":"paul@example.com",
     "username" : "paul.the.boss",
     "href" : "/users/548bf7d0e3bfc67d4d7c2cb6",
     "facebookUserId" : "15678999767689"
  },
  "tokens": {
     "user" : {
        "id" : "548bf7d0e3bfc67d4d7c2cb6",
        "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
     },
     "id":"fuEyvqImw2xbywewZAUHkFMo8xJO7eSOAOjkaRRSTTfzRTqdblN65Mx7O2JhmzVc"
  }
}
```
```
HTTP/1.1 409 Conflict
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"An user with facebookUserId 15678999767689 already exists"
   }
}
```

```
HTTP/1.1 400 Bad Request
Content-Type: application/vnd.api+json
{
    "errors" : [
        {"title":"error with field /email : invalid email address"},
        {"title":"error with field /password : invalid sha256"}
    ]
}
```

# Login by email
## Request
```
PUT /tokens/J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje
X-Access-Token: J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje

{
  "users": {
    "email" : "paul@example.com",
    "password" : "6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"
  }
}
```

## Results
```
HTTP/1.1 200 OK
Content-Type: application/vnd.api+json

{
  "users": {
     "id" : "548bf7d0e3bfc67d4d7c2cb6",
     "email":"paul@example.com",
     "username" : "paul.the.boss",
     "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
  },
  "tokens": {
     "user" : {
              "id" : "548bf7d0e3bfc67d4d7c2cb6",
              "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
     },
     "id":"fuEyvqImw2xbywewZAUHkFMo8xJO7eSOAOjkaRRSTTfzRTqdblN65Mx7O2JhmzVc"
  }
}
```
```
HTTP/1.1 404 Not Found
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"No user account for this email"
   }
}
```
```
HTTP/1.1 401 Unauthorized
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"Incorrect password"
   }
}
```

# Login by username
## Request
```
PUT /tokens/J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje
X-Access-Token: J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje

{
  "users": {
    "username" : "paul.the.boss",
    "password" : "6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e"
  }
}
```

## Results
```
HTTP/1.1 200 OK
Content-Type: application/vnd.api+json

{
  "users": {
     "id" : "548bf7d0e3bfc67d4d7c2cb6",
     "email":"paul@example.com",
     "username" : "paul.the.boss",
     "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
  },
  "tokens": {
     "user" : {
              "id" : "548bf7d0e3bfc67d4d7c2cb6",
              "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
     },
     "id":"fuEyvqImw2xbywewZAUHkFMo8xJO7eSOAOjkaRRSTTfzRTqdblN65Mx7O2JhmzVc"
  }
}
```
```
HTTP/1.1 404 Not Found
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"No user account for this username"
   }
}
```
```
HTTP/1.1 401 Unauthorized
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"Incorrect password"
   }
}
```

# Login by facebook
## Request
```
PUT /tokens/J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje
X-Access-Token: J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje

{
  "users": {
     "facebookToken" : "CAADOJKDKJLDJMLDSKdsklokfdmlgdfpo878UNKJnkjnNKJNKJNIUG6T56789jtyR5YU7Hyuitusijfddslofj6789ihjnkjTYChg56789NJUhbvgtcyr67yuvgft",
  }
}
```

## Results
```
HTTP/1.1 200 OK
Content-Type: application/vnd.api+json

{
  "users": {
     "id" : "548bf7d0e3bfc67d4d7c2cb6",
     "email":"paul@example.com",
     "username" : "paul.the.boss",
     "href" : "/users/548bf7d0e3bfc67d4d7c2cb6",
     "facebookUserId" : "15678999767689"
  },
  "tokens": {
     "user" : {
              "id" : "548bf7d0e3bfc67d4d7c2cb6",
              "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
     },
     "id":"fuEyvqImw2xbywewZAUHkFMo8xJO7eSOAOjkaRRSTTfzRTqdblN65Mx7O2JhmzVc"
  }
}
```

```
HTTP/1.1 404 Not Found
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"No user account for the facebookUserId associated to this facebookToken"
   }
}
```

# Check if an email is already used
## Request

```
GET /emails/paul@example.com
X-Access-Token: J1WAnckPPHm8jX8Abvc61VvVBY1cmqCnGSr46oUqvOY0MUsO4u0dhWlGipHHZaje
```

## Results

```
HTTP/1.1 200 OK
Content-Type: application/vnd.api+json

{
    "emails": {
       "id" : "paul@example.com",
       "state" : "registered"
    }
}
```

```
HTTP/1.1 404 Not Found
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"Email not found"
   }
}
```

# Retrieve user information
## Request
```
GET /users/548bf7d0e3bfc67d4d7c2cb6
X-Access-Token: fuEyvqImw2xbywewZAUHkFMo8xJO7eSOAOjkaRRSTTfzRTqdblN65Mx7O2JhmzVc
```

## Results
```
HTTP/1.1 200 OK
Content-Type: application/vnd.api+json

{
  "users": {
     "id" : "548bf7d0e3bfc67d4d7c2cb6",
     "email":"paul@example.com",
     "username" : "paul.the.boss",
     "href" : "/users/548bf7d0e3bfc67d4d7c2cb6"
  }
}
```
```
HTTP/1.1 403 Forbidden
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"You can only retrieve the user associated with the token"
   }
}
```
```
HTTP/1.1 401 Unauthorized
Content-Type: application/vnd.api+json

{
   "errors": {
      "title":"Unknown token fuEyvqImw2xbywewZAUHkFMo8xJO7eSOAOjkaRRSTTfzRTqdblN65Mx7O2JhmzVc"
   }
}
```