# Quality Gates

Sentimentum uses a lightweight static quality gate in CI. The gate follows the lecture thresholds for practical code metrics:

| Metric | Warning | Stopper |
|---|---:|---:|
| Method cyclomatic complexity | > 10 | > 20 |
| WMC per class | > 30 | > 60 |
| CBO per class | > 10 | > 20 |
| DIT | > 5 | > 7 |
| Duplication | > 5% | > 15% |
| Methods per class | > 15 | > 20 |

The CI pipeline fails only on stopper values. Warning values are printed in the report so a reviewer can see quality risks before they become blockers.

Run locally:

```bash
python3 tools/quality_gate.py
```

Reports are generated in `build/reports/quality-gate.md` and `build/reports/quality-gate.json`.

## Warning Emails

The CI job sends an email when the quality report contains warnings. Configure these GitHub Actions secrets:

| Secret | Purpose |
|---|---|
| `SMTP_HOST` | SMTP server host |
| `SMTP_PORT` | SMTP server port |
| `SMTP_USERNAME` | SMTP login |
| `SMTP_PASSWORD` | SMTP password or app password |
| `QUALITY_ALERT_FROM` | Sender email |
| `QUALITY_ALERT_TO` | Recipient email |

If the secrets are not configured, the email step is skipped without failing the pipeline.
