# Cloud APIM - Otoroshi Biscuit Studio

## Plugin configurations

### Verifier Plugin configuration

Here is a demo configuration :

```js
  {
    "verifier_ref": "YOUR_BISCUIT_VERIFIER_REF",
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
  }
```
