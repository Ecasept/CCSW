# Copilot Instructions

This document provides guidance for AI coding agents to effectively contribute to this project.

## Project Overview

This project is a "Cookie Clicker" stock watcher with three main components. It monitors the in-game stock market and does not perform any actions in the game.

-   `analyzer/`: A Python application that monitors the game, performs OCR, and sends data to the server.
-   `server/`: A Cloudflare Worker (TypeScript) that serves as the backend, using Supabase for the database.
-   `app/`: An Android application for monitoring and interacting with the watcher.

These components are in a monorepo structure.

## Key Architectural Concepts

-   **Data Flow**: The `analyzer` is the primary data source. It captures game state via screenshots and OCR, then sends it to the `server`. The `app` communicates with the `server` to display this data.
-   **Backend**: The `server` is a Cloudflare Worker. Business logic is written in TypeScript. It uses Supabase for its database and edge functions.
-   **Configuration**: The main configuration for the Python watcher is in `config.jsonc` at the root of the project. The server's configuration is in `server/wrangler.jsonc`.

## Component Overview

### Analyzer (`analyzer/`)

The analyzer is a Python application responsible for:

-   Taking screenshots of the Cookie Clicker game window.
-   Performing OCR on the screenshots to extract stock market data. This can be done locally using `easyocr` or by sending the image to the server's AI processing endpoint (`/api/process`). The mode is controlled by the `USE_LEGACY_OCR` flag in `config.jsonc`.
-   Sending the extracted data to the server's `/api/update` endpoint.
-   It also includes a simulation feature (`analyzer/src/simulation/`) which runs a one-time TypeScript simulation to generate stock market quartile data, used for analysis.

### Server (`server/`)

The server is a Cloudflare Worker written in TypeScript that acts as the backend. Its features include:

-   Receiving and storing stock data from the `analyzer` via the `/api/update` endpoint.
-   Providing an API for the Android `app` to fetch data, such as `/api/goodHistory` for historical stock data.
-   An endpoint (`/api/process`) for AI-powered OCR of game screenshots.
-   A registration endpoint (`/api/register`) likely for the Android app to register for push notifications.
-   Using Supabase for database storage and edge functions, including sending push notifications via Firebase.

### App (`app/`)

The app is an Android application that allows the user to:

-   Monitor the stock market data collected by the `analyzer`.
-   View historical data and trends for each stock, fetched from the server.
-   Receive push notifications about stock market changes.

## Project-Specific Conventions

-   **Logging**: The Python analyzer has a dedicated logging setup in `analyzer/src/logger.py`. Use this logger for any new logging in the analyzer. Logs are written to `analyzer/logs/`.

## Key Files and Directories

-   `config.jsonc`: Main configuration for the watcher.
-   `analyzer/main.py`: Entry point for the Python analyzer.
-   `analyzer/src/ocr.py`: Contains the logic for both local OCR and sending images for AI processing.
-   `analyzer/src/simulation/`: Contains the one-time stock market simulation.
-   `server/src/index.ts`: Main entry point for the Cloudflare Worker, defining the API routes.
-   `server/wrangler.jsonc`: Configuration for the Cloudflare Worker.
-   `server/supabase/`: Contains database schema and edge functions for Supabase.
-   `app/`: The Android application.
