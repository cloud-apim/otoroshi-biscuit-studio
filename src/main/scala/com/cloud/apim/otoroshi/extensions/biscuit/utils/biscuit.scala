package com.cloud.apim.otoroshi.extensions.biscuit.utils

import otoroshi.env.Env

object BiscuitUtils {

  def readOrWrite(method: String): String = {
    method match {
      case "DELETE" => "write"
      case "GET" => "read"
      case "HEAD" => "read"
      case "OPTIONS" => "read"
      case "PATCH" => "write"
      case "POST" => "write"
      case "PUT" => "write"
      case _ => "none"
    }
  }

  def handleBiscuitErrors(error: org.biscuitsec.biscuit.error.Error)(implicit env: Env): String = {
    error match {
      case err: org.biscuitsec.biscuit.error.Error.FormatError.UnknownPublicKey => {
        s"UnknownPublicKey"
      }

      case err: org.biscuitsec.biscuit.error.Error.FormatError.DeserializationError => {
        s"DeserializationError - ${err.e}"
      }

      case err: org.biscuitsec.biscuit.error.Error.FailedLogic => {
        s"FailedLogic - ${err.error.toString}"
      }

      case err: org.biscuitsec.biscuit.error.Error.InvalidAuthorityIndex => {
        s"InvalidAuthorityIndex - ${err.index}"
      }

      case err: org.biscuitsec.biscuit.error.Error.InvalidBlockIndex => {
        s"InvalidBlockIndex - expected:  ${err.expected} found: ${err.found}"
      }

      case err: org.biscuitsec.biscuit.error.Error.MissingSymbols => {
        s"Biscuit MissingSymbols"
      }

      case err: org.biscuitsec.biscuit.error.Error.Timeout => {
        s"Biscuit Timeout"
      }

      case err: org.biscuitsec.biscuit.error.Error.InvalidType => {
        s"Biscuit InvalidType"
      }

      case err: org.biscuitsec.biscuit.error.Error.InternalError => {
        "Biscuit InternalError"
      }

      case _ => {
        error.toString
      }
    }
  }
  def getAlgo(algoName: String): biscuit.format.schema.Schema.PublicKey.Algorithm = {
    algoName.toUpperCase match {
      case "ED25519" => biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519
      //      case "SECP256R1" => biscuit.format.schema.Schema.PublicKey.Algorithm.SECP256R1 -- waiting for support in java lib
      case _ => biscuit.format.schema.Schema.PublicKey.Algorithm.Ed25519
    }
  }
}