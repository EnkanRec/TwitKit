/**
 * guid: "{"GUID"}"
 * GUID: hex{8}"-"hex{4}"-"hex{4}"-"hex{4}"-"hex{12}
 * hex: [0-9A-Fa-f]
 */
export type uuid = string

/**
 * guid: "{"GUID"}"
 * GUID: \d+"-"(0\d|1[0-2])"-"([0-2]\d|3[0-1])"T"([0-1]\d|2[0-3])":"[0-5]\d":"[0-5]\d"."\d{3}"+"(0\d|1[0-3])":"[0,3]0
 */
export type ISO8601 = string