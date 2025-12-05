
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
-- 4️⃣ USERS TABLE (Updated with Profile Fields)
-- =======================================================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY, -- Internal ID
                       user_uuid UUID NOT NULL UNIQUE, -- External ID
                       email VARCHAR(255) UNIQUE NOT NULL,
                       user_password TEXT NOT NULL,
                       company_id BIGINT, -- FK uses internal ID
                       status VARCHAR(20) NOT NULL DEFAULT 'active', -- 'pending', 'active', 'banned'
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       last_login TIMESTAMP,
                       token_version INT DEFAULT 0,
                       verified BOOLEAN DEFAULT FALSE, -- For ID card verification

                       CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(id)
                           ON DELETE SET NULL
);

truncate table users restart identity cascade ;


CREATE TABLE user_profile (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL UNIQUE, -- 1-to-1 link to users table
                              first_name VARCHAR(100),
                              last_name VARCHAR(100),
                              user_name VARCHAR(100),
                              email VARCHAR(100),
                              phone_number VARCHAR(100),
                              user_profile VARCHAR(255),
                              cover_proile VARCHAR(255),
                              address TEXT,
                              city TEXT,
                              country TEXT,
                              Postal_Code VARCHAR(255),
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
drop table user_profile cascade ;

CREATE TABLE role (

                      id BIGSERIAL PRIMARY KEY,
                      role_uuid UUID NOT NULL UNIQUE,
                      role_name VARCHAR(100) UNIQUE NOT NULL, -- e.g., 'Platform Admin', 'Company Admin', 'Seller', 'Buyer'
                      description TEXT,
    -- 'platform' roles are managed by you, 'company' roles are managed by Company Admins
                      scope VARCHAR(20) NOT NULL DEFAULT 'platform' CHECK (scope IN ('platform', 'company')),
                      company_id BIGINT, -- NULL for 'platform' roles, set for 'company' roles
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_role_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
);
CREATE TABLE permission (
                            id BIGSERIAL PRIMARY KEY,
                            permission_uuid UUID NOT NULL UNIQUE,
    -- e.g., 'CREATE_PRODUCT', 'EDIT_PRODUCT', 'DELETE_COMPANY', 'VIEW_FINANCE_AUDIT'
                            permission_name VARCHAR(100) UNIQUE NOT NULL,
                            description TEXT,
    -- 'Product', 'User', 'Finance' - for grouping in an admin UI
                            resource_group VARCHAR(50),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permission (
                                 role_id BIGINT NOT NULL,
                                 permission_id BIGINT NOT NULL,
                                 CONSTRAINT fk_roleperm_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_roleperm_permission FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE,
                                 PRIMARY KEY (role_id, permission_id) -- Composite key
);

CREATE TABLE user_role (
                           user_id BIGINT NOT NULL,
                           role_id BIGINT NOT NULL,
                           CONSTRAINT fk_userrole_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                           CONSTRAINT fk_userrole_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
                           PRIMARY KEY (user_id, role_id) -- Composite key
);

-- (Tracks all important actions for security and compliance)
-- =======================================================
CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT, -- The user who performed the action
                           user_uuid UUID, -- The user's external ID
                           action VARCHAR(100) NOT NULL, -- e.g., 'LOGIN', 'CREATE_PRODUCT', 'UPDATE_USER_STATUS'
                           target_type VARCHAR(50), -- e.g., 'Product', 'User', 'Company'
                           target_id UUID, -- The UUID of the object that was changed
                           details JSONB, -- Can store 'before' and 'after' state
                           ip_address VARCHAR(50),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 24 USER INVITATION TABLE (NEW)
-- (Allows company admins to invite new users to their company)
-- =======================================================
CREATE TABLE user_invitation (
                                 id BIGSERIAL PRIMARY KEY,
                                 invitation_uuid UUID NOT NULL UNIQUE, -- Used in the invitation link
                                 email VARCHAR(255) NOT NULL,
                                 company_id BIGINT NOT NULL,
                                 role_id BIGINT NOT NULL, -- The role the user will get
                                 invited_by_user_id BIGINT NOT NULL,
                                 status VARCHAR(20) NOT NULL DEFAULT 'pending', -- 'pending', 'accepted', 'expired'
                                 expires_at TIMESTAMP NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_invite_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_invite_role FOREIGN KEY (role_id) REFERENCES role(id),
                                 CONSTRAINT fk_invite_user FOREIGN KEY (invited_by_user_id) REFERENCES users(id)
);

-- 25 COMPANY SETTINGS TABLE (NEW)
-- (A key-value store for company-specific settings)
-- =======================================================
CREATE TABLE company_settings (
                                  id BIGSERIAL PRIMARY KEY,
                                  company_id BIGINT NOT NULL,
                                  setting_name VARCHAR(100) NOT NULL,
                                  setting_value TEXT,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_settings_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
                                  UNIQUE(company_id, setting_name) -- Each company can only have one value for each setting
);


-- Add the foreign key from company to users (after users table is created)
ALTER TABLE company ADD CONSTRAINT fk_company_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;


-- =======================================================
-- 5️⃣ PRODUCT TABLE (MODIFIED)
-- =======================================================
CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         product_uuid UUID NOT NULL UNIQUE,
                         product_name VARCHAR(255) NOT NULL,
                         company_id BIGINT, -- Stays nullable
                         seller_id BIGINT NOT NULL,
                         category_id BIGINT NOT NULL,
                         description TEXT NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- REMOVED: price DECIMAL(10,2) NOT NULL,
    -- REMOVED: stock_quantity INT NOT NULL,
                         CONSTRAINT fk_product_company FOREIGN KEY (company_id) REFERENCES company(id),
                         CONSTRAINT fk_product_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id)
);
drop table orders cascade ;
-- =======================================================
-- NEW: PRODUCT OPTION TABLE (e.g., "Size", "Color")
-- =======================================================
CREATE TABLE product_option (
                                id BIGSERIAL PRIMARY KEY,
                                option_uuid UUID NOT NULL UNIQUE,
                                product_id BIGINT NOT NULL,
                                option_name VARCHAR(100) NOT NULL,
                                CONSTRAINT fk_option_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
                                UNIQUE(product_id, option_name) -- A product can't have "Size" twice
);

-- =======================================================
-- NEW: PRODUCT OPTION VALUE TABLE (e.g., "Small", "Red")
-- =======================================================
CREATE TABLE product_option_value (
                                      id BIGSERIAL PRIMARY KEY,
                                      value_uuid UUID NOT NULL UNIQUE,
                                      option_id BIGINT NOT NULL,
                                      value_name VARCHAR(100) NOT NULL,
                                      CONSTRAINT fk_value_option FOREIGN KEY (option_id) REFERENCES product_option(id) ON DELETE CASCADE,
                                      UNIQUE(option_id, value_name) -- An option can't have "Red" twice
);

-- =======================================================
-- NEW: PRODUCT VARIANT TABLE (The SKU: e.g., "Small-Red-Shirt")
-- =======================================================
CREATE TABLE product_variant (
                                 id BIGSERIAL PRIMARY KEY,
                                 variant_uuid UUID NOT NULL UNIQUE,
                                 product_id BIGINT NOT NULL,
                                 sku VARCHAR(255) UNIQUE, -- Stock Keeping Unit
                                 price DECIMAL(10,2) NOT NULL,
                                 stock_quantity INT NOT NULL,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- =======================================================
-- NEW: PRODUCT VARIANT VALUES JUNCTION TABLE
-- (Links a Variant to its values)
-- =======================================================
CREATE TABLE product_variant_values (
                                        variant_id BIGINT NOT NULL,
                                        value_id BIGINT NOT NULL,
                                        CONSTRAINT fk_link_variant FOREIGN KEY (variant_id) REFERENCES product_variant(id) ON DELETE CASCADE,
                                        CONSTRAINT fk_link_value FOREIGN KEY (value_id) REFERENCES product_option_value(id) ON DELETE CASCADE,
                                        PRIMARY KEY (variant_id, value_id) -- Composite key
);


-- =======================================================
-- 6️⃣ PRODUCT IMAGE TABLE (MODIFIED)
-- =======================================================
CREATE TABLE product_image (
                               id BIGSERIAL PRIMARY KEY,
                               image_uuid UUID NOT NULL UNIQUE,
                               product_id BIGINT NOT NULL,
                               image_url VARCHAR(512) NOT NULL,
                               is_main BOOLEAN DEFAULT FALSE,
                               sort_order INT,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               variant_id BIGINT,
                               option_value_id BIGINT,

                               CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
                               CONSTRAINT fk_image_variant FOREIGN KEY (variant_id) REFERENCES product_variant(id) ON DELETE SET NULL,
                               CONSTRAINT fk_image_option_value FOREIGN KEY (option_value_id) REFERENCES product_option_value(id) ON DELETE SET NULL
);

-- ... (rest of Schema.sql is unchanged) ...
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
                        CONSTRAINT fk_verify_submitter FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE CASCADE,
                        CONSTRAINT fk_verify_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL
);

drop table verify;
truncate table users restart identity cascade ;
-- =======================================================
-- 8️⃣ SHIPPING ADDRESS TABLE
-- =======================================================
CREATE TABLE shipping_address
(
    id             BIGSERIAL PRIMARY KEY,        -- Internal ID
    address_uuid   UUID         NOT NULL UNIQUE, -- External ID
    user_id        BIGINT       NOT NULL,        -- FK uses internal ID
    full_name      VARCHAR(255) NOT NULL,
    phone_number   VARCHAR(20) NOT NULL ,
    address_line_1 VARCHAR(255) NOT NULL,
    city           VARCHAR(100) NOT NULL,
    country        VARCHAR(100) NOT NULL,
    is_default     BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_shipping_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE TABLE product_tier_pricing (
                                      id BIGSERIAL PRIMARY KEY,
                                      product_variant_id BIGINT NOT NULL,
                                      min_quantity INT NOT NULL, -- e.g., 50
                                      price DECIMAL(10,2) NOT NULL, -- e.g., $9.00 (instead of $10.00)
                                      CONSTRAINT fk_tier_variant FOREIGN KEY (product_variant_id) REFERENCES product_variant(id) ON DELETE CASCADE
);
truncate review restart identity cascade ;
-- =======================================================
-- 9️⃣ ORDER TABLE
-- =======================================================
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,                -- Internal ID
                        order_uuid UUID NOT NULL UNIQUE,         -- External ID
                        user_id BIGINT NOT NULL,                 -- FK uses internal ID

    -- ========================================================================
    -- 🚚 SNAPSHOT ADDRESS FIELDS (The "Truth" for this specific order)
    -- ========================================================================
    -- We copy these values from the shipping_address table at the moment of checkout.
                        shipping_full_name VARCHAR(255) NOT NULL,
                        shipping_address_line_1 VARCHAR(255) NOT NULL,
                        shipping_city VARCHAR(100) NOT NULL,
                        shipping_country VARCHAR(100) NOT NULL,
                        shipping_phone VARCHAR(20),              -- Essential for couriers (grab from user profile or address)

    -- Optional: Keep a link to the original ID for "Buy Again" features,
    -- but make it nullable in case the user deletes the address later.
                        original_shipping_address_id BIGINT,

    -- ========================================================================
    -- 💰 ORDER DETAILS
    -- ========================================================================
                        total_price DECIMAL(10,2) NOT NULL,
                        status VARCHAR(20) DEFAULT 'pending',    -- 'pending', 'shipped', 'delivered', 'cancelled'
                        payment_method VARCHAR(50),

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- ========================================================================
    -- 🔗 CONSTRAINTS
    -- ========================================================================
                        CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- If the user deletes the address from their book, set this column to NULL
    -- (so the order history doesn't break).
                        CONSTRAINT fk_order_original_address FOREIGN KEY (original_shipping_address_id) REFERENCES shipping_address(id) ON DELETE SET NULL
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
                        CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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
                              CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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

CREATE TABLE favorite (
                          id BIGSERIAL PRIMARY KEY,
                          favorite_uuid UUID NOT NULL UNIQUE,
                          user_id BIGINT NOT NULL,
                          product_id BIGINT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
                          UNIQUE(user_id, product_id) -- Prevent duplicate favorites
);


CREATE TABLE rfq_request (
                             id BIGSERIAL PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             quantity_required INT NOT NULL,
                             target_price DECIMAL(10,2),
                             description TEXT,
                             status VARCHAR(20) DEFAULT 'open', -- 'open', 'closed'
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rfq_bid (
                         id BIGSERIAL PRIMARY KEY,
                         rfq_id BIGINT NOT NULL,
                         seller_id BIGINT NOT NULL,
                         price DECIMAL(10,2) NOT NULL,
                         message TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_rfq_bid_rfq FOREIGN KEY (rfq_id) REFERENCES rfq_request(id),
                         CONSTRAINT fk_rfq_bid_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE disputes (
                          id BIGSERIAL PRIMARY KEY,
                          order_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL, -- The buyer creating the dispute
                          reason VARCHAR(50) NOT NULL, -- e.g., 'NOT_RECEIVED', 'DAMAGED', 'FAKE'
                          description TEXT,
                          status VARCHAR(20) DEFAULT 'OPEN', -- 'OPEN', 'RESOLVED', 'REJECTED'
                          admin_comment TEXT, -- If admin steps in
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_dispute_order FOREIGN KEY (order_id) REFERENCES orders(id),
                          CONSTRAINT fk_dispute_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE dispute_evidence (
                                  id BIGSERIAL PRIMARY KEY,
                                  dispute_id BIGINT NOT NULL,
                                  image_url VARCHAR(255) NOT NULL,
                                  CONSTRAINT fk_evidence_dispute FOREIGN KEY (dispute_id) REFERENCES disputes(id)
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


delete from users where id = 4 ;
SELECT * FROM verify WHERE target_id = 'b618ed47-f6d2-4550-b894-d368f2e7d52c';