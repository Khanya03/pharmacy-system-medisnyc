# MediSync

## Audit log database migration

Existing databases need a one-time column change because `ddl-auto=update` does not widen columns:

```sql
ALTER TABLE audit_logs MODIFY COLUMN details TEXT;
```

Alternatively, use `VARCHAR(2000)` for the `details` column.

## Payments and revenue

Card payments run through the simulated gateway in demo mode; use the documented test cards
for payment-flow testing. The backend stores payment metadata and the card's last four digits,
never the card number or CVV. Legacy orders created before payment tracking have a null payment
status and are treated as unpaid; they are excluded from revenue until paid.

Demo card outcomes: `4242 4242 4242 4242` (Visa, approved), `5555 5555 5555 4444`
(Mastercard, approved), `4000 0000 0000 0002` (declined), `4000 0000 0000 9995`
(insufficient funds), and `4000 0000 0000 0069` (expired card). Use any future expiry
and a 3- or 4-digit CVV for these test numbers.
