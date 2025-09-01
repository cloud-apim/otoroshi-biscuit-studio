# Cloud APIM - Otoroshi Biscuit Studio

[![Tests Status](https://github.com/cloud-apim/otoroshi-biscuit-studio/actions/workflows/test-extension.yml/badge.svg)](https://github.com/cloud-apim/otoroshi-biscuit-studio/actions/workflows/test-extension.yml)

![](/images/otoroshi-biscuit-studio-logo.png)

The Otoroshi Biscuit Studio is set of [Otoroshi](https://github.com/MAIF/otoroshi) plugins to use Biscuit Tokens into your Gateway.

## Reference Documentation

The Otoroshi Biscuit Studio documentation is available here : https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/overview

## Installation 

Follow this guide in order to get Otoroshi with the Biscuit Studio Extension.
### Download Otoroshi

[ ![Download Otoroshi](https://img.shields.io/github/release/MAIF/otoroshi.svg) ](https://github.com/MAIF/otoroshi/releases/download/v17.5.0/otoroshi.jar)

```sh
curl -L -o otoroshi.jar 'https://github.com/MAIF/otoroshi/releases/download/v17.5.0/otoroshi.jar'
```

### Download the Biscuit Studio extension
  
[![Download Otoroshi Biscuit Studio extension](https://img.shields.io/github/release/cloud-apim/otoroshi-biscuit-studio.svg) ](https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/download/0.0.18/otoroshi-biscuit-studio-0.0.18.jar)

You can download the latest release of `otoroshi-biscuit-studio` from https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/latest

```sh
curl -L -o biscuit-studio-extension.jar 'https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/download/0.0.18/otoroshi-biscuit-studio-0.0.18.jar'
```

### Run Otoroshi with the Biscuit Studio Extension

```sh
java -cp "./biscuit-studio-extension.jar:./otoroshi.jar" -Dotoroshi.adminLogin=admin -Dotoroshi.adminPassword=password -Dotoroshi.storage=file play.core.server.ProdServerStart
```

Open http://otoroshi.oto.tools:8080/ in your browser

Default Otoroshi UI credentials : admin / password
## Entities
  - [KeyPairs](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/entities/keypairs)
  - [Verifiers](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/entities/verifiers)
  - [Attenuators](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/entities/attenuators)
  - [RBAC Policies](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/entities/rbac)
  - [Remote Facts Loader](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/entities/remotefacts)
  - [Forges](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/entities/forges)
## Plugins
 - [Verifier plugin](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/plugins/verifiers)
 - [Attenuator plugin](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/plugins/attenuators)
 - [Client Credentials plugin](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/plugins/clientcredentials)
 - [Biscuit to User plugin](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/plugins/biscuit-user-extractor)
 - [User to Biscuit plugin](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/plugins/user-to-biscuit)
 - [Biscuit to ApiKey Bridge Plugin](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/plugins/apikeybridge)
 - [Public Keys exposition](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/plugins/keypairsexposition)

### Biscuit Studio API Reference

[API Reference](https://cloud-apim.github.io/otoroshi-biscuit-studio/docs/api)
