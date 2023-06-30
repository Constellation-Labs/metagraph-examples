import express, { Express, Request, Response } from "express";
import bodyParser from "body-parser";
import dotenv from "dotenv";

import addressRoutes from './src/routes/address.route'

dotenv.config();

const app: Express = express();
const port = process.env.PORT ?? 8000;

app.use(bodyParser.json());
app.use(
  bodyParser.urlencoded({
    extended: true,
  })
);

app.get("/", (req: Request, res: Response) => {
  return res.json({ message: "ok" });
});

app.use('/addresses', addressRoutes);

app.listen(port, () => {
  console.log(`⚡️[server]: Server is running at http://localhost:${port}`);
});
