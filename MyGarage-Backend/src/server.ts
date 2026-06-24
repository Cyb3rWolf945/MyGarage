import app from "./app";
import { env } from "./config/env";

app.listen(env.PORT, () => {
  const publicDomain = process.env.RAILWAY_PUBLIC_DOMAIN;

  if (publicDomain) {
    console.log(`🚗 MyGarage API running on https://${publicDomain}`);
    console.log(`   Health: https://${publicDomain}/api/health`);
  } else {
    console.log(`🚗 MyGarage API running on http://localhost:${env.PORT}`);
    console.log(`   Health: http://localhost:${env.PORT}/api/health`);
  }
});

export default app;
