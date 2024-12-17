# Cloud APIM - Otoroshi Biscuit Studio

## Plugin configurations

### Verifier Plugin configuration

Here is a demo configuration :

```js
  {
    "verifier_ref": "YOUR_BISCUIT_VERIFIER_ENTITY_REF",
    "rbac_ref": "RBAC_POLICY_ENTITY_REF" // optional
    "enforce": false, // true or false
    "extractor_type": "header", // header, query or cookies
    "extractor_name": "Authorization"
  }
```

### Attenuator Plugin configuration

Here is a demo configuration :

```js
  {
    "ref": "YOUR_BISCUIT_ATTENUATOR_REF",
    "extractor_type": "header", // header, query or cookies
    "extractor_name": "Authorization"
    "token_replace_loc": "header", // header, query or cookies
    "token_replace_name": "biscuit_token"
  }
```
