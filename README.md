# Cloud APIM - Otoroshi Biscuit Studio

The Otoroshi Biscuit Studio is set of Otoroshi plugins to use Biscuit Tokens into your Gateway. To know more about it, go to [documentation](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/overview)

## Installation 

## Create your first entity : KeyPair

## creates the keypair entity
```js
curl -X POST -H 'Content-Type: application/json' 'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs' -u admin-api-apikey-id:admin-api-apikey-secret -d '{
  "id": "biscuit-keypair_e42033bc-f181-485f-857d-576e4728f6f9",
  "name": "KeyPair from Otoroshi API",
  "description": "A Biscuit KeyPair created from Otoroshi API",
  "pubKey": "cc9f2638b2aa05ffe72a85f91875ac451ddc8995c8ddc39290fdaeb473314dcb",
  "privKey": "0e8a4d1cf07b6ee07b12f7658b6e784b590da13b97ab5c0140764a84373c8619",
  "tags": [],
  "kind": "biscuit.extensions.cloud-apim.com/BiscuitKeyPair"
}'
```

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

### Biscuit Studio API Reference

[API Reference](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/api)