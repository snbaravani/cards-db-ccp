USE cards;
INSERT INTO customer_card_summary (summary_id, customer_id, program_id, programme_name, card_status, fraud_flag, expiry_date, credit_outstanding) VALUES
('dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Platinum Rewards', 'ACTIVE', false, '2028-06-30', 1250.75),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Gold Cashback', 'BLOCKED', true, '2027-02-28', 320.00),
('ffffffff-ffff-ffff-ffff-ffffffffffff', '33333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Frequent Flyer Elite', 'INACTIVE', false, '2029-12-31', 0.00),
('ffffffff-ffff-ffff-ffff-ffffffffffff', '444444444-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Frequent Flyer Elite', 'ACTIVE', false, '2029-12-31', 0.00);
