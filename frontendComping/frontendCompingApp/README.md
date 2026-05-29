# FrontendCompingApp

This is the frontend application for the Comping project, built with Angular 19.

## Development server

To start a local development server, run:

```bash
npm install
npm start
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Integration with Backend

By default, the frontend expects the backend to be running on `http://localhost:8087`. You can configure this in the environment files if needed.

## Key Features implemented
- User registration and login
- Sorting and filtering of camping trips
- Team formation and recommendations
- Product store with Stripe payment integration
- Dashboard for admins and users

## Building

To build the project run:

```bash
npm run build
```

This will compile your project and store the build artifacts in the `dist/` directory.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
npm test
```
