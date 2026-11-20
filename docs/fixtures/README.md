# Vendor fixtures

All vendor interaction in tests runs against recorded fixtures stored in this directory,
never against real vendor endpoints. Real endpoints only ever appear behind configuration
filled in by the human via environment variables.

Rules:

- One file per recorded response, named `<provider>/<scenario>.json`.
- Fixtures must be scrubbed of tokens, user identifiers, and any secret-like values.
- If a needed fixture does not exist, create one and mark it `synthetic` in the file name
  (e.g. `withings/measure-synthetic.json`) and in the agent report.
