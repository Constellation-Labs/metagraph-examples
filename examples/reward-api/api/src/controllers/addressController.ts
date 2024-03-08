import { Request, Response } from "express";
import { dag4 } from "@stardust-collective/dag4";

type Address = {
  address: string;
  date: Date;
};

const addresses: Address[] = [];

const getAddresses = (
  req: Request,
  res: Response
): Response<any, Record<string, any>> => {
  const sortedAddresses = addresses.sort((a, b) => {
    return new Date(b.date).getTime() - new Date(a.date).getTime();
  });

  return res.status(200).json(sortedAddresses.slice(0, 20));
};

const postAddress = (
  req: Request,
  res: Response
): Response<any, Record<string, any>> => {
  const { body } = req;

  if (!body.address || !dag4.account.validateDagAddress(body.address)) {
    return res.status(400).json({ error: "Invalid DAG Address" });
  }

  addresses.push({
    address: body.address,
    date: new Date(),
  });

  return res.status(201).json({});
};

export { getAddresses, postAddress };
