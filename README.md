# Grails Jetty Plugin

## Introduction

Grails Jetty Plugin now supports multiple Jetty Container and Servlet API versions. Refer to the below matrix to find out the correct plugin version to be used for your requirements.

    Grails Version    Servlet API Version     Jetty Container Version   Jetty Plugin Version
    2.2.x             2.5.x                   7.x                       2.0.x
    2.3.x             3.0.x                   9.0.x                     3.0.x

## Usage

In your `BuildConfig.groovy` add the following lines under the plugins section:

    build ":jetty:3.0.0"

## Configuration

The following can be configured in `BuildConfig.groovy`:

- `grails.jetty.keystorePath`
- `grails.jetty.keystorePassword`
