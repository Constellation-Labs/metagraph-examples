/* eslint-disable @typescript-eslint/triple-slash-reference */
/// <reference types="react-scripts" />

/**
 * ISO 8601 formatted date
 *
 * E.g. "2022-12-14T15:35:48Z"
 *
 * @see {@link https://en.wikipedia.org/wiki/ISO_8601}
 */
type IISODate = string;

/**
 * ISO 8601 formatted interval
 *
 * E.g. "P1DT1H"
 *
 * @see {@link https://en.wikipedia.org/wiki/ISO_8601}
 */
type IISODuration = string;

/**
 * Encoded JWT Token
 */
type IJWTToken = string;

type JSONScalar = null | boolean | number | string;

type JSONValue =
  | Record<string | number, JSONScalar>
  | JSONScalar
  | JSONScalar[];

/**
 * Decimal.js parsable decimal
 */
type IDecimalString = string;
