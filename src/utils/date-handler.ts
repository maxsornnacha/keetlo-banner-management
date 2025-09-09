export function formatDate(
  input: string | number | Date | null | undefined,
  {
    timezone = "Asia/Bangkok",
    dateFormat = "DD-MM-YYYY",
  }: { timezone?: string; dateFormat?: string } = {}
): string {
  if (!input) return "";

  const date = new Date(input);
  if (["YYYY/MM/DD", "DD/MM/YYYY", "MM/DD/YYYY"].includes(dateFormat)) {
    const parts = new Intl.DateTimeFormat("en-GB", {
      timeZone: timezone,
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).formatToParts(date);

    const day = parts.find((p) => p.type === "day")?.value ?? "";
    const month = parts.find((p) => p.type === "month")?.value ?? "";
    const year = parts.find((p) => p.type === "year")?.value ?? "";

    switch (dateFormat) {
      case "YYYY/MM/DD":
        return `${year}/${month}/${day}`;
      case "DD/MM/YYYY":
        return `${day}/${month}/${year}`;
      case "MM/DD/YYYY":
        return `${month}/${day}/${year}`;
    }
  }

  return new Intl.DateTimeFormat("en-US", {
    timeZone: timezone,
    dateStyle: "medium",
  }).format(date);
}

export function formatDateTime(
  input: string | number | Date | null | undefined,
  {
    timezone = "Asia/Bangkok",
    locale = "en-US",
    dateFormat = "DD/MM/YYYY",     // you can pass a date-only format
  }: { timezone?: string; locale?: string; dateFormat?: string } = {}
): string {
  if (!input) return "";

  const date = new Date(input);

  // Build parts (24h for HH/mm/ss)
  const parts24 = new Intl.DateTimeFormat(locale, {
    timeZone: timezone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).formatToParts(date);

  // 12h parts for hh and AM/PM
  const parts12 = new Intl.DateTimeFormat(locale, {
    timeZone: timezone,
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: true,
  }).formatToParts(date);

  const lookup: Record<string, string> = {};
  for (const p of parts24) lookup[p.type] = p.value;
  for (const p of parts12) {
    if (p.type === "dayPeriod") lookup["A"] = p.value.toUpperCase(); // AM/PM
    if (p.type === "hour") lookup["hh"] = p.value;                   // 12h hour
  }

  // If dateFormat has NO time tokens, append " hh:mm A"
  const hasTimeTokens = /(H{1,2}|h{1,2}|m{1,2}|s{1,2}|A|a)/.test(dateFormat);
  const effectiveFormat = hasTimeTokens ? dateFormat : `${dateFormat} hh:mm A`;

  return effectiveFormat
    .replace(/YYYY/g, lookup.year ?? "")
    .replace(/MM/g, lookup.month ?? "")
    .replace(/DD/g, lookup.day ?? "")
    .replace(/HH/g, lookup.hour ?? "")   // 24h
    .replace(/hh/g, lookup.hh ?? "")     // 12h
    .replace(/mm/g, lookup.minute ?? "")
    .replace(/ss/g, lookup.second ?? "")
    .replace(/A/g, lookup.A ?? "");
}
