-- -- =======================================================
-- -- 1️⃣ MAIN CATEGORY TABLE
-- -- =======================================================
-- CREATE TABLE main_category (
--                                main_category_id UUID PRIMARY KEY,
--                                main_category_name VARCHAR(100) UNIQUE NOT NULL,
--                                description TEXT,
--                                icon_url VARCHAR(512),
--                                status VARCHAR(20) DEFAULT 'active',
--                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );
--
-- -- =======================================================
-- -- 2️⃣ CATEGORY TABLE
-- -- =======================================================
-- CREATE TABLE category (
--                           category_id UUID PRIMARY KEY,
--                           category_name VARCHAR(100) NOT NULL,
--                           description TEXT,
--                           icon_url VARCHAR(512),
--                           main_category_id UUID NOT NULL,
--                           level INT NOT NULL DEFAULT 1,
--                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                           CONSTRAINT fk_category_main FOREIGN KEY (main_category_id) REFERENCES main_category(main_category_id)
--                               ON DELETE CASCADE
-- );
--
-- -- =======================================================
-- -- 3️⃣ COMPANY TABLE
-- -- =======================================================
-- CREATE TABLE company (
--                          company_id UUID PRIMARY KEY,
--                          company_name VARCHAR(255) UNIQUE NOT NULL,
--                          industry_type VARCHAR(100) NOT NULL,
--                          logo_url VARCHAR(512),
--                          description TEXT,
--                          verified BOOLEAN DEFAULT FALSE,
--                          created_by UUID,
--                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                          status VARCHAR(20) DEFAULT 'active',
--                          rating_average DECIMAL(3,2) DEFAULT 0.00
-- );
--
-- -- =======================================================
-- -- 4️⃣ USERS TABLE
-- -- =======================================================
-- CREATE TABLE users (
--                        user_id UUID PRIMARY KEY,
--                        user_name VARCHAR(255),
--                        email VARCHAR(255) UNIQUE NOT NULL,
--                        user_password TEXT NOT NULL,
--                        role VARCHAR(50) NOT NULL DEFAULT 'buyer',  -- 'admin_platform', 'admin_company', 'seller_company', 'buyer'
--                        company_id UUID,
--                        status VARCHAR(20) NOT NULL DEFAULT 'active',
--                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                        last_login TIMESTAMP,
--                        CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(company_id)
--                            ON DELETE SET NULL
-- );
--
-- -- =======================================================
-- -- 5️⃣ PRODUCT TABLE
-- -- =======================================================
-- CREATE TABLE product (
--                          product_id UUID PRIMARY KEY,
--                          product_name VARCHAR(255) NOT NULL,
--                          company_id UUID NOT NULL,
--                          seller_id UUID NOT NULL,
--                          category_id UUID NOT NULL,
--                          description TEXT NOT NULL,
--                          price DECIMAL(10,2) NOT NULL,
--                          stock_quantity INT NOT NULL,
--                          is_active BOOLEAN DEFAULT TRUE,
--                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                          CONSTRAINT fk_product_company FOREIGN KEY (company_id) REFERENCES company(company_id),
--                          CONSTRAINT fk_product_seller FOREIGN KEY (seller_id) REFERENCES users(user_id),
--                          CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(category_id)
-- );
--
-- -- =======================================================
-- -- 6️⃣ PRODUCT IMAGE TABLE
-- -- =======================================================
-- CREATE TABLE product_image (
--                                image_id UUID PRIMARY KEY,
--                                product_id UUID NOT NULL,
--                                image_url VARCHAR(512) UNIQUE NOT NULL,
--                                is_main BOOLEAN DEFAULT FALSE,
--                                sort_order INT,
--                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                                CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES product(product_id)
--                                    ON DELETE CASCADE
-- );
--
-- -- =======================================================
-- -- 7️⃣ VERIFY TABLE
-- -- =======================================================
-- CREATE TABLE verify (
--                         verify_id UUID PRIMARY KEY,
--                         target_type VARCHAR(50) NOT NULL, -- 'user', 'company', 'product'
--                         target_id UUID NOT NULL,
--                         submitted_by UUID NOT NULL,
--                         verify_documents JSONB,
--                         status VARCHAR(20) NOT NULL DEFAULT 'pending', -- 'pending', 'approved', 'rejected', 'expired'
--                         reviewed_by UUID,
--                         reviewed_at TIMESTAMP,
--                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                         remarks TEXT,
--                         CONSTRAINT fk_verify_submitter FOREIGN KEY (submitted_by) REFERENCES users(user_id),
--                         CONSTRAINT fk_verify_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
-- );
--
-- -- =======================================================
-- -- 8️⃣ SHIPPING ADDRESS TABLE
-- -- =======================================================
-- CREATE TABLE shipping_address (
--                                   address_id UUID PRIMARY KEY,
--                                   user_id UUID NOT NULL,
--                                   full_name VARCHAR(255) NOT NULL,
--                                   address_line_1 VARCHAR(255) NOT NULL,
--                                   city VARCHAR(100) NOT NULL,
--                                   country VARCHAR(100) NOT NULL,
--                                   is_default BOOLEAN DEFAULT FALSE,
--                                   CONSTRAINT fk_shipping_user FOREIGN KEY (user_id) REFERENCES users(user_id)
-- );
--
-- -- =======================================================
-- -- 9️⃣ ORDER TABLE
-- -- =======================================================
-- CREATE TABLE orders (
--                         order_id UUID PRIMARY KEY,
--                         user_id UUID NOT NULL,
--                         shipping_address_id UUID NOT NULL,
--                         total_price DECIMAL(10,2) NOT NULL,
--                         status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'shipped', 'delivered', 'cancelled'
--                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                         payment_method VARCHAR(50),
--                         CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(user_id),
--                         CONSTRAINT fk_order_address FOREIGN KEY (shipping_address_id) REFERENCES shipping_address(address_id)
-- );
--
-- -- =======================================================
-- -- 🔟 ORDER ITEMS TABLE
-- -- =======================================================
-- CREATE TABLE order_items (
--                              item_id UUID PRIMARY KEY,
--                              order_id UUID NOT NULL,
--                              product_id UUID NOT NULL,
--                              quantity INT NOT NULL,
--                              price_per_unit DECIMAL(10,2) NOT NULL,
--                              total_item_price DECIMAL(10,2) NOT NULL,
--                              CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
--                                  ON DELETE CASCADE,
--                              CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES product(product_id)
-- );
--
-- -- =======================================================
-- -- 1️⃣1️⃣ PAYMENT TABLE
-- -- =======================================================
-- CREATE TABLE payment (
--                          payment_id UUID PRIMARY KEY,
--                          order_id UUID NOT NULL,
--                          amount DECIMAL(10,2) NOT NULL,
--                          payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                          transaction_id VARCHAR(255) UNIQUE NOT NULL,
--                          status VARCHAR(20) DEFAULT 'success',  -- 'success', 'failed', 'refunded'
--                          CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
-- );
--
-- -- =======================================================
-- -- 1️⃣2️⃣ REVIEW TABLE
-- -- =======================================================
-- CREATE TABLE review (
--                         review_id UUID PRIMARY KEY,
--                         product_id UUID NOT NULL,
--                         user_id UUID NOT NULL,
--                         rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
--                         review_text TEXT,
--                         review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                         helpful_votes INT DEFAULT 0,
--                         CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(product_id),
--                         CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(user_id)
-- );
--
-- -- =======================================================
-- -- 1️⃣3️⃣ NOTIFICATION TABLE
-- -- =======================================================
-- CREATE TABLE notification (
--                               notification_id UUID PRIMARY KEY,
--                               user_id UUID NOT NULL,
--                               notification_type VARCHAR(50) NOT NULL, -- 'order_status', 'promotion', 'system'
--                               title VARCHAR(255) NOT NULL,
--                               message TEXT NOT NULL,
--                               is_read BOOLEAN DEFAULT FALSE,
--                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                               link_url VARCHAR(512),
--                               CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
-- );
--
-- --main categories
-- INSERT INTO main_category (main_category_name)
-- VALUES
--     ('Electronics'),
--     ('Fashion'),
--     ('Home & Garden'),
--     ('Beauty & Health'),
--     ('Sports & Outdoors'),
--     ('Toys & Hobbies'),
--     ('Automotive'),
--     ('Food & Grocery');
--
-- --sub categories
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Mobile Phones', main_category_id, 2 FROM main_category WHERE main_category_name = 'Electronics';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Laptops & Computers', main_category_id, 2 FROM main_category WHERE main_category_name = 'Electronics';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Cameras & Photography', main_category_id, 2 FROM main_category WHERE main_category_name = 'Electronics';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Audio & Video', main_category_id, 2 FROM main_category WHERE main_category_name = 'Electronics';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Wearables', main_category_id, 2 FROM main_category WHERE main_category_name = 'Electronics';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Gaming Consoles', main_category_id, 2 FROM main_category WHERE main_category_name = 'Electronics';
--
-- -- ================= Fashion Subcategories =================
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Men''s Clothing', main_category_id, 2 FROM main_category WHERE main_category_name = 'Fashion';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Women''s Clothing', main_category_id, 2 FROM main_category WHERE main_category_name = 'Fashion';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Shoes', main_category_id, 2 FROM main_category WHERE main_category_name = 'Fashion';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Bags & Accessories', main_category_id, 2 FROM main_category WHERE main_category_name = 'Fashion';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Jewelry', main_category_id, 2 FROM main_category WHERE main_category_name = 'Fashion';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Watches', main_category_id, 2 FROM main_category WHERE main_category_name = 'Fashion';
--
-- -- ================= Home & Garden Subcategories =================
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Furniture', main_category_id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Kitchen & Dining', main_category_id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Home Decor', main_category_id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Bedding & Bath', main_category_id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Lighting', main_category_id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Garden Supplies', main_category_id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
--
-- -- ================= Beauty & Health Subcategories =================
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Skincare', main_category_id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Haircare', main_category_id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Makeup', main_category_id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Personal Care', main_category_id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Health Supplements', main_category_id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Fragrances', main_category_id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
--
-- -- ================= Sports & Outdoors Subcategories =================
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Fitness Equipment', main_category_id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Outdoor Gear', main_category_id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Bicycles & Accessories', main_category_id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Camping & Hiking', main_category_id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Sports Apparel', main_category_id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
--
-- -- ================= Toys & Hobbies Subcategories =================
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Action Figures', main_category_id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Board Games', main_category_id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Model Kits', main_category_id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Educational Toys', main_category_id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Musical Instruments', main_category_id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
--
-- -- ================= Automotive Subcategories =================
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Car Accessories', main_category_id, 2 FROM main_category WHERE main_category_name = 'Automotive';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Motorbike Accessories', main_category_id, 2 FROM main_category WHERE main_category_name = 'Automotive';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Auto Parts', main_category_id, 2 FROM main_category WHERE main_category_name = 'Automotive';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Car Electronics', main_category_id, 2 FROM main_category WHERE main_category_name = 'Automotive';
--
-- -- ================= Food & Grocery Subcategories =================
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Snacks & Confectionery', main_category_id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Beverages', main_category_id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Health Supplements', main_category_id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Fresh Food', main_category_id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
-- INSERT INTO category (category_name, main_category_id, level)
-- SELECT 'Packaged Food', main_category_id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';

-- =======================================================
-- 1️⃣ MAIN CATEGORY TABLE
-- =======================================================
CREATE TABLE main_category (
                               id BIGSERIAL PRIMARY KEY, -- Internal ID for FKs
                               main_category_uuid UUID NOT NULL UNIQUE, -- External ID for API
                               main_category_name VARCHAR(100) UNIQUE NOT NULL,
                               description TEXT,
                               icon_url VARCHAR(512),
                               status VARCHAR(20) DEFAULT 'active',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =======================================================
-- 2️⃣ CATEGORY TABLE
-- =======================================================
CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY, -- Internal ID
                          category_uuid UUID NOT NULL UNIQUE, -- External ID
                          category_name VARCHAR(100) NOT NULL,
                          description TEXT,
                          icon_url VARCHAR(512),
                          main_category_id BIGINT NOT NULL, -- FK uses internal ID
                          level INT NOT NULL DEFAULT 1,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_category_main FOREIGN KEY (main_category_id) REFERENCES main_category(id)
                              ON DELETE CASCADE
);

-- =======================================================
-- 3️⃣ COMPANY TABLE
-- =======================================================
CREATE TABLE company (
                         id BIGSERIAL PRIMARY KEY, -- Internal ID
                         company_uuid UUID NOT NULL UNIQUE, -- External ID
                         company_name VARCHAR(255) UNIQUE NOT NULL,
                         industry_type VARCHAR(100) NOT NULL,
                         logo_url VARCHAR(512),
                         description TEXT,

    -- Address Fields --
                         address_line_1 VARCHAR(255),
                         city VARCHAR(100),
                         state_province VARCHAR(100),
                         postal_code VARCHAR(20),
                         country VARCHAR(100),

                         verified BOOLEAN DEFAULT FALSE,
                         created_by BIGINT, -- FK to users(id), nullable
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         status VARCHAR(20) DEFAULT 'active',
                         rating_average DECIMAL(3,2) DEFAULT 0.00,
                         max_sellers INT DEFAULT 3 -- For seller limit
);

-- =======================================================
-- 4️⃣ USERS TABLE
-- =======================================================
-- =======================================================
-- 4️⃣ USERS TABLE (Updated with Profile Fields)
-- =======================================================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY, -- Internal ID
                       user_uuid UUID NOT NULL UNIQUE, -- External ID

    -- Profile fields
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       user_name VARCHAR(100),
                       user_profile VARCHAR(255),
                       dob DATE,
                       address TEXT,

    -- Auth fields
                       email VARCHAR(255) UNIQUE NOT NULL,
                       user_password TEXT NOT NULL,
                       role VARCHAR(50) NOT NULL DEFAULT 'buyer',  -- 'admin_platform', 'admin_company', 'seller_company', 'buyer'
                       company_id BIGINT, -- FK uses internal ID
                       status VARCHAR(20) NOT NULL DEFAULT 'active',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login TIMESTAMP,

    -- --- NEW FIELDS ---
                       phone_number VARCHAR(20) UNIQUE, -- Added for phone number
                       phone_verified BOOLEAN DEFAULT FALSE, -- Added for phone verification
                       verified BOOLEAN DEFAULT FALSE, -- Added for ID card verification
    -- --- END NEW FIELDS ---

                       CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(id)
                           ON DELETE SET NULL
);
drop table users cascade ;

-- Add the foreign key from company to users (after users table is created)
ALTER TABLE company ADD CONSTRAINT fk_company_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;


-- =======================================================
-- 5️⃣ PRODUCT TABLE
-- =======================================================
CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY, -- Internal ID
                         product_uuid UUID NOT NULL UNIQUE, -- External ID
                         product_name VARCHAR(255) NOT NULL,
                         company_id BIGINT NOT NULL, -- FK uses internal ID
                         seller_id BIGINT NOT NULL, -- FK uses internal ID
                         category_id BIGINT NOT NULL, -- FK uses internal ID
                         description TEXT NOT NULL,
                         price DECIMAL(10,2) NOT NULL,
                         stock_quantity INT NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_product_company FOREIGN KEY (company_id) REFERENCES company(id),
                         CONSTRAINT fk_product_seller FOREIGN KEY (seller_id) REFERENCES users(id),
                         CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
);

-- =======================================================
-- 6️⃣ PRODUCT IMAGE TABLE
-- =======================================================
CREATE TABLE product_image (
                               id BIGSERIAL PRIMARY KEY, -- Internal ID
                               image_uuid UUID NOT NULL UNIQUE, -- External ID
                               product_id BIGINT NOT NULL, -- FK uses internal ID
                               image_url VARCHAR(512) UNIQUE NOT NULL,
                               is_main BOOLEAN DEFAULT FALSE,
                               sort_order INT,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES product(id)
                                   ON DELETE CASCADE
);

-- =======================================================
-- 7️⃣ VERIFY TABLE
-- =======================================================
CREATE TABLE verify (
                        id BIGSERIAL PRIMARY KEY, -- Internal ID
                        verify_uuid UUID NOT NULL UNIQUE, -- External ID
                        target_type VARCHAR(50) NOT NULL, -- 'user', 'company', 'product'
                        target_id UUID NOT NULL, -- Polymorphic key, stores the external UUID of the target
                        submitted_by BIGINT NOT NULL, -- FK uses internal users(id)
                        verify_documents JSONB, -- We will store the ID card URL here
                        status VARCHAR(20) NOT NULL DEFAULT 'pending', -- 'pending', 'approved', 'rejected'
                        reviewed_by BIGINT, -- FK uses internal users(id)
                        reviewed_at TIMESTAMP,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        remarks TEXT,
                        CONSTRAINT fk_verify_submitter FOREIGN KEY (submitted_by) REFERENCES users(id),
                        CONSTRAINT fk_verify_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id)
);
drop table verify cascade ;
truncate table users restart identity cascade ;
-- =======================================================
-- 8️⃣ SHIPPING ADDRESS TABLE
-- =======================================================
CREATE TABLE shipping_address (
                                  id BIGSERIAL PRIMARY KEY, -- Internal ID
                                  address_uuid UUID NOT NULL UNIQUE, -- External ID
                                  user_id BIGINT NOT NULL, -- FK uses internal ID
                                  full_name VARCHAR(255) NOT NULL,
                                  address_line_1 VARCHAR(255) NOT NULL,
                                  city VARCHAR(100) NOT NULL,
                                  country VARCHAR(100) NOT NULL,
                                  is_default BOOLEAN DEFAULT FALSE,
                                  CONSTRAINT fk_shipping_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =======================================================
-- 9️⃣ ORDER TABLE
-- =======================================================
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY, -- Internal ID
                        order_uuid UUID NOT NULL UNIQUE, -- External ID
                        user_id BIGINT NOT NULL, -- FK uses internal ID
                        shipping_address_id BIGINT NOT NULL, -- FK uses internal ID
                        total_price DECIMAL(10,2) NOT NULL,
                        status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'shipped', 'delivered', 'cancelled'
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        payment_method VARCHAR(50),
                        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id),
                        CONSTRAINT fk_order_address FOREIGN KEY (shipping_address_id) REFERENCES shipping_address(id)
);

-- =======================================================
-- 🔟 ORDER ITEMS TABLE
-- =======================================================
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY, -- Internal ID
                             item_uuid UUID NOT NULL UNIQUE, -- External ID
                             order_id BIGINT NOT NULL, -- FK uses internal ID
                             product_id BIGINT NOT NULL, -- FK uses internal ID
                             quantity INT NOT NULL,
                             price_per_unit DECIMAL(10,2) NOT NULL,
                             total_item_price DECIMAL(10,2) NOT NULL,
                             CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(id)
                                 ON DELETE CASCADE,
                             CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES product(id)
);

-- =======================================================
-- 1️⃣1️⃣ PAYMENT TABLE
-- =======================================================
CREATE TABLE payment (
                         id BIGSERIAL PRIMARY KEY, -- Internal ID
                         payment_uuid UUID NOT NULL UNIQUE, -- External ID
                         order_id BIGINT NOT NULL, -- FK uses internal ID
                         amount DECIMAL(10,2) NOT NULL,
                         payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         transaction_id VARCHAR(255) UNIQUE NOT NULL,
                         status VARCHAR(20) DEFAULT 'success',  -- 'success', 'failed', 'refunded'
                         CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- =======================================================
-- 1️⃣2️⃣ REVIEW TABLE
-- =======================================================
CREATE TABLE review (
                        id BIGSERIAL PRIMARY KEY, -- Internal ID
                        review_uuid UUID NOT NULL UNIQUE, -- External ID
                        product_id BIGINT NOT NULL, -- FK uses internal ID
                        user_id BIGINT NOT NULL, -- FK uses internal ID
                        rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                        review_text TEXT,
                        review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        helpful_votes INT DEFAULT 0,
                        CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(id),
                        CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =======================================================
-- 1️⃣3️⃣ NOTIFICATION TABLE
-- =======================================================
CREATE TABLE notification (
                              id BIGSERIAL PRIMARY KEY, -- Internal ID
                              notification_uuid UUID NOT NULL UNIQUE, -- External ID
                              user_id BIGINT NOT NULL, -- FK uses internal ID
                              notification_type VARCHAR(50) NOT NULL, -- 'order_status', 'promotion', 'system'
                              title VARCHAR(255) NOT NULL,
                              message TEXT NOT NULL,
                              is_read BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              link_url VARCHAR(512),
                              CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE otp_number (
                            id BIGSERIAL PRIMARY KEY,
                            email VARCHAR(255) NOT NULL UNIQUE, -- Ensures one active OTP per email
                            otp_code VARCHAR(10) NOT NULL,
                            expires_at TIMESTAMP NOT NULL
);
CREATE TABLE sms_otp (
                         id BIGSERIAL PRIMARY KEY,
                         phone_number VARCHAR(255) NOT NULL UNIQUE,
                         otp_code VARCHAR(10) NOT NULL,
                         expires_at TIMESTAMP NOT NULL
);

-- =======================================================
-- DATA INSERTION (Requires main_category data to exist first)
-- =======================================================

--main categories (EXAMPLE: You must provide UUIDs from your app)
-- INSERT INTO main_category (main_category_uuid, main_category_name)
-- VALUES
--     ('your-uuid-from-spring-1', 'Electronics'),
--     ('your-uuid-from-spring-2', 'Fashion'),
--     ('your-uuid-from-spring-3', 'Home & Garden'),
--     ('your-uuid-from-spring-4', 'Beauty & Health'),
--     ('your-uuid-from-spring-5', 'Sports & Outdoors'),
--     ('your-uuid-from-spring-6', 'Toys & Hobbies'),
--     ('your-uuid-from-spring-7', 'Automotive'),
--     ('your-uuid-from-spring-8', 'Food & Grocery');

--sub categories (These are corrected to use the internal 'id')
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Mobile Phones', id, 2 FROM main_category WHERE main_category_name = 'Electronics';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Laptops & Computers', id, 2 FROM main_category WHERE main_category_name = 'Electronics';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Cameras & Photography', id, 2 FROM main_category WHERE main_category_name = 'Electronics';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Audio & Video', id, 2 FROM main_category WHERE main_category_name = 'Electronics';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Wearables', id, 2 FROM main_category WHERE main_category_name = 'Electronics';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Gaming Consoles', id, 2 FROM main_category WHERE main_category_name = 'Electronics';

-- ================= Fashion Subcategories =================
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Men''s Clothing', id, 2 FROM main_category WHERE main_category_name = 'Fashion';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Women''s Clothing', id, 2 FROM main_category WHERE main_category_name = 'Fashion';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Shoes', id, 2 FROM main_category WHERE main_category_name = 'Fashion';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Bags & Accessories', id, 2 FROM main_category WHERE main_category_name = 'Fashion';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Jewelry', id, 2 FROM main_category WHERE main_category_name = 'Fashion';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Watches', id, 2 FROM main_category WHERE main_category_name = 'Fashion';

-- ================= Home & Garden Subcategories =================
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Furniture', id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Kitchen & Dining', id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Home Decor', id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Bedding & Bath', id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Lighting', id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Garden Supplies', id, 2 FROM main_category WHERE main_category_name = 'Home & Garden';

-- ================= Beauty & Health Subcategories =================
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Skincare', id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Haircare', id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Makeup', id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Personal Care', id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Health Supplements', id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Fragrances', id, 2 FROM main_category WHERE main_category_name = 'Beauty & Health';

-- ================= Sports & Outdoors Subcategories =================
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Fitness Equipment', id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Outdoor Gear', id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Bicycles & Accessories', id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Camping & Hiking', id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Sports Apparel', id, 2 FROM main_category WHERE main_category_name = 'Sports & Outdoors';

-- ================= Toys & Hobbies Subcategories =================
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Action Figures', id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Board Games', id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Model Kits', id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Educational Toys', id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Musical Instruments', id, 2 FROM main_category WHERE main_category_name = 'Toys & Hobbies';

-- ================= Automotive Subcategories =================
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Car Accessories', id, 2 FROM main_category WHERE main_category_name = 'Automotive';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Motorbike Accessories', id, 2 FROM main_category WHERE main_category_name = 'Automotive';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Auto Parts', id, 2 FROM main_category WHERE main_category_name = 'Automotive';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Car Electronics', id, 2 FROM main_category WHERE main_category_name = 'Automotive';

-- ================= Food & Grocery Subcategories =================
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Snacks & Confectionery', id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Beverages', id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Health Supplements', id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Fresh Food', id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';
INSERT INTO category (category_name, main_category_id, level)
SELECT 'Packaged Food', id, 2 FROM main_category WHERE main_category_name = 'Food & Grocery';