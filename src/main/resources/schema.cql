
CREATE TABLE IF NOT EXISTS beas.orders (
        orderNumber TEXT PRIMARY KEY,
        retailerMoniker TEXT,
        orderInfo TEXT,
        classification TEXT);