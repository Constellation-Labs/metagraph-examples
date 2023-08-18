declare global {
  const FrontendAppEnvironment: Record<string, string> | undefined;
}

const getEnvOrError = <T extends string = string>(
  ...alternatives: string[]
): T => {
  const env = {
    ...(process.env ?? {}),
    ...(process.env.FRONTEND_APP_ENVIRONMENT
      ? FrontendAppEnvironment ?? {}
      : {})
  };

  for (const alternative of alternatives) {
    const result = env[alternative];
    if (result) {
      return result as T;
    }
  }

  throw new Error(
    `Unable to find env variable by alternatives => [${alternatives.join(
      ', '
    )}]`
  );
};

export { getEnvOrError };
