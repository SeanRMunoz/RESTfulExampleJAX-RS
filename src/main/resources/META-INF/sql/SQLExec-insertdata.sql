INSERT INTO customer (ID, FIRST_NAME, LAST_NAME) VALUES
(59, 'ANGELA', 'JENKINS'),
(101, 'MRS', 'NO-PHONES'),
(151, 'FRANK-UPDATE', 'ZAPPA'),
(201, 'JANE', 'SMITH'),
(204, 'JANE', 'SMITH-DOE');

INSERT INTO address (ID, CITY, STREET, ID_CUSTOMER) VALUES
(60, 'SECOND-CITY', '123 Your Street', 59),
(102, 'Sacramento', '123 Your Street', 101),
(152, 'Sacramento', '123 ANY CIRCLE', 151),
(202, 'Roseville', '123 Your Street', 201),
(205, 'Sacramento', '123 Your Street', 204);

INSERT INTO phone_number (ID, NUM, TYPE, ID_CUSTOMER) VALUES
(61, '916-321-1212', 'Work', 59),
(153, '800-567-1525 x221', 'TOLL-FREE', 151),
(154, '916-555-5656', 'Work', 151),
(203, '916-321-1212', 'Work', 201),
(206, '916-321-1111', 'Work', 204);

INSERT INTO sequence (SEQ_NAME, SEQ_COUNT) VALUES
('SEQ_GEN', 250);
