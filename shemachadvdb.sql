-- Step 1: Create the database and use it
CREATE DATABASE shemachadvdb;
GO
USE shemachadvdb;
GO

-- Step 2: Government table
CREATE TABLE government (
    government_id INT PRIMARY KEY,
    government_name VARCHAR(50) NOT NULL DEFAULT 'The Ethiopian National Food Control System'      
);

-- Step 3: Government contacts
CREATE TABLE government_contacts (
    government_id INT NOT NULL,               
    gov_contact_info VARCHAR(25) NOT NULL UNIQUE,  
    PRIMARY KEY (government_id, gov_contact_info), 
    FOREIGN KEY (government_id) REFERENCES government(government_id)
);

-- Step 4: Woreda table
CREATE TABLE woreda (
    woreda_id INT PRIMARY KEY,            
    woreda_name VARCHAR(20) NOT NULL UNIQUE, 
    government_id INT NOT NULL, 
    region_name VARCHAR(50) DEFAULT 'Addis Ababa',
    FOREIGN KEY (government_id) REFERENCES government(government_id)
);

-- Step 5: Kebele table
CREATE TABLE kebele (
    kebele_id INT PRIMARY KEY,         
    kebele_name VARCHAR(20) NOT NULL,  
    woreda_id INT NOT NULL,               
    FOREIGN KEY (woreda_id) REFERENCES woreda(woreda_id)
);

-- Step 6: Shemachoche table
CREATE TABLE shemachoche (
    shemachoche_id INT PRIMARY KEY,        
    shemachoche_name VARCHAR(30) NOT NULL, 
    kebele_id INT NOT NULL,              
    FOREIGN KEY (kebele_id) REFERENCES kebele(kebele_id)
);

-- Step 7: Home table
CREATE TABLE home (
    home_id INT PRIMARY KEY,            
    home_owner_name VARCHAR(30) NOT NULL CHECK(home_owner_name NOT LIKE '%[^A-Za-z ]%'),
    number_of_family INT NOT NULL,          
    shemachoche_id INT NOT NULL,                
    FOREIGN KEY (shemachoche_id) REFERENCES shemachoche(shemachoche_id)
);

-- Step 8: Employee table
CREATE TABLE employee (
    emp_id INT PRIMARY KEY,                   
    shemachoche_id INT NOT NULL,                
    emp_name VARCHAR(50) NOT NULL,               
    emp_position VARCHAR(20) NOT NULL,          
    emp_gender CHAR(1) NOT NULL CHECK (emp_gender IN ('M', 'F')), 
    emp_age INT NOT NULL CHECK (emp_age BETWEEN 18 AND 65), 
    emp_salary DECIMAL(10,2) NOT NULL CHECK (emp_salary > 0),
    FOREIGN KEY (shemachoche_id) REFERENCES shemachoche(shemachoche_id)
);

-- Step 9: Employee contacts
CREATE TABLE employee_contacts (
    emp_id INT NOT NULL,                     
    emp_contact_info VARCHAR(25) NOT NULL,          
    PRIMARY KEY (emp_id, emp_contact_info),         
    FOREIGN KEY (emp_id) REFERENCES employee(emp_id)
);

-- Step 10: Item table
CREATE TABLE item (
    item_id INT PRIMARY KEY,                         
    item_name VARCHAR(15) NOT NULL,               
    is_restricted CHAR(3) NOT NULL CHECK (is_restricted IN ('Yes', 'No')),
    category VARCHAR(15) NOT NULL,             
    max_per_month INT NOT NULL CHECK (max_per_month >= 0)
);

-- Step 11: Purchase table
CREATE TABLE purchase (
    purchase_id INT PRIMARY KEY,                      
    home_id INT NULL,                                 
    item_id INT NOT NULL,                                
    quantity INT NOT NULL CHECK (quantity > 0),        
    purchase_date DATE NOT NULL,                     
    buyer_type VARCHAR(15) NOT NULL CHECK (buyer_type IN ('Homeowner', 'Non-homeowner')), 
    FOREIGN KEY (home_id) REFERENCES home(home_id),    
    FOREIGN KEY (item_id) REFERENCES item(item_id)
    -- ON DELETE CASCADE -- Optional: enable if you want related purchases to be removed with home/item
);

-- Step 12: Insert government
INSERT INTO government (government_id) 
VALUES (1);

-- Step 13: Insert government contacts
INSERT INTO government_contacts (gov_contact_info, government_id) 
VALUES 
('0966778899',1),
('0977889900',1),
('0988990011',1);

-- Step 14: Insert woredas
INSERT INTO woreda (woreda_id, woreda_name, government_id) 
VALUES 
(1, 'Woreda 1',1),
(2, 'Woreda 2',1),
(3, 'Woreda 3',1);

-- Step 15: Insert kebeles
INSERT INTO kebele (kebele_id, kebele_name, woreda_id) 
VALUES 
(1, 'Kebele 1', 1),
(2, 'Kebele 2', 2),
(3, 'Kebele 3', 3);

-- Step 16: Insert shemachoches
INSERT INTO shemachoche (shemachoche_id, shemachoche_name, kebele_id) 
VALUES 
(1, 'Shemachoche 1', 1),
(2, 'Shemachoche 2', 2),
(3, 'Shemachoche 3', 3);

-- Step 17: Insert homes
INSERT INTO home (home_id, home_owner_name, number_of_family, shemachoche_id) 
VALUES 
(1, 'Abebe Alemayehu', 7, 1),
(2, 'Kebedech Zeleke', 6, 2),
(3, 'Nuhamin Kefale', 8, 3),
(4, 'Hermela Mulugeta', 9, 1),
(5, 'Eleni Melaku', 10, 2),
(6, 'Melat Samson', 11, 3),
(7, 'Mariamawit Alemseged', 13, 1),
(8, 'Munira Tebarek', 12, 2),
(9, 'Kalkidan Awoke', 14, 3),
(10,'Abera Lema', 10, 1);

-- Step 18: Insert employees
INSERT INTO employee (emp_id, shemachoche_id, emp_name, emp_position, emp_gender, emp_age, emp_salary) 
VALUES 
(1, 1, 'Employee 1', 'Manager', 'M', 30, 5000.00),
(2, 2, 'Employee 2', 'Clerk', 'F', 25, 3000.00),
(3, 3, 'Employee 3', 'Supervisor', 'M', 35, 4000.00);

-- Step 19: Insert employee contacts
INSERT INTO employee_contacts (emp_id, emp_contact_info) 
VALUES 
(1, '0999001122'),
(2, '0977889900'),
(3, '0988990011');

-- Step 20: Insert items
INSERT INTO item (item_id, item_name, is_restricted, category, max_per_month) 
VALUES 
(1, 'Sugar', 'Yes', 'Food', 5),
(2, 'Oil', 'Yes', 'Food', 3),
(3, 'Macaroni', 'No', 'Food', 10),
(4, 'Meser', 'No', 'Food', 15),
(5, 'Flour', 'No', 'Food', 20);

-- Step 21: Insert purchases
INSERT INTO purchase (purchase_id, home_id, item_id, quantity, purchase_date, buyer_type) 
VALUES 
(1, 1, 1, 2, '2023-10-01', 'Homeowner'),
(2, 2, 2, 3, '2023-10-02', 'Homeowner'),
(3, NULL, 3, 5, '2023-10-03', 'Non-homeowner'),
(4, 4, 1, 1, '2023-10-04', 'Homeowner'),
(5, 5, 2, 2, '2023-10-05', 'Homeowner'),
(6, NULL, 3, 4, '2023-10-06', 'Non-homeowner'),
(7, 7, 1, 3, '2023-10-07', 'Homeowner'),
(8, 8, 2, 1, '2023-10-08', 'Homeowner'),
(9, NULL, 3, 6, '2023-10-09', 'Non-homeowner'),
(10, 10, 1, 4, '2023-10-10', 'Homeowner');
DROP PROCEDURE IF EXISTS Insert_Purchase_WithQuotaCheck;
GO

-- Then paste your CREATE PROCEDURE here

-- Step 22: Create the procedure for inserting with rules
CREATE PROCEDURE Insert_Purchase_WithQuotaCheck
    @purchase_id INT,
    @home_id INT = NULL,
    @item_id INT,
    @quantity INT,
    @purchase_date DATE,
    @buyer_type VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @quota INT = 5;  -- Example monthly quota for non-homeowners
    DECLARE @is_restricted BIT;

    -- Check if item is restricted
    SELECT @is_restricted = 
    CASE 
        WHEN is_restricted = 'Yes' THEN 1
        ELSE 0
    END
FROM item
WHERE item_id = @item_id;


    -- Handle Non-homeowners
    IF @buyer_type = 'Non-homeowner'
    BEGIN
        -- Prevent purchase of restricted items
        IF @is_restricted = 1
        BEGIN
            RAISERROR('Restricted items can only be purchased by homeowners.', 16, 1);
            RETURN;
        END

        -- Check total quantity bought this month by non-homeowners for this item
        DECLARE @total_quantity INT = (
            SELECT ISNULL(SUM(Quantity), 0)
            FROM Purchases
            WHERE HomeID IS NULL
              AND ItemID = @item_id
              AND MONTH(PurchaseDate) = MONTH(@purchase_date)
              AND YEAR(PurchaseDate) = YEAR(@purchase_date)
        );

        IF (@total_quantity + @quantity) > @quota
        BEGIN
            RAISERROR('Non-homeowners exceeded monthly quota for this item.', 16, 1);
            RETURN;
        END

        -- Insert the purchase for non-homeowner
        INSERT INTO Purchases (PurchaseID, HomeID, ItemID, Quantity, PurchaseDate)
        VALUES (@purchase_id, NULL, @item_id, @quantity, @purchase_date);
    END

    -- Handle Homeowners
    ELSE IF @buyer_type = 'Homeowner'
    BEGIN
        IF @home_id IS NULL
        BEGIN
            RAISERROR('Home ID is required for homeowners.', 16, 1);
            RETURN;
        END

        -- Insert the purchase for homeowner
        INSERT INTO Purchases (PurchaseID, HomeID, ItemID, Quantity, PurchaseDate)
        VALUES (@purchase_id, @home_id, @item_id, @quantity, @purchase_date);
    END

    ELSE
    BEGIN
        RAISERROR('Invalid buyer type. Must be Homeowner or Non-homeowner.', 16, 1);
        RETURN;
    END
END
ALTER PROCEDURE Insert_Purchase_WithQuotaCheck
    @purchase_id INT,
    @home_id INT = NULL,
    @item_id INT,
    @quantity INT,
    @purchase_date DATE,
    @buyer_type VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @quota INT = 5;  -- Monthly quota for non-homeowners
    DECLARE @is_restricted BIT;

    -- Convert 'Yes'/'No' from item.is_restricted to 1/0
    SELECT @is_restricted = 
        CASE 
            WHEN is_restricted = 'Yes' THEN 1
            ELSE 0
        END
    FROM item
    WHERE item_id = @item_id;

    -- Non-homeowner logic
    IF @buyer_type = 'Non-homeowner'
    BEGIN
        IF @is_restricted = 1
        BEGIN
            RAISERROR('Restricted items can only be purchased by homeowners.', 16, 1);
            RETURN;
        END

        DECLARE @total_quantity INT = (
            SELECT ISNULL(SUM(quantity), 0)
            FROM purchase
            WHERE home_id IS NULL
              AND item_id = @item_id
              AND MONTH(purchase_date) = MONTH(@purchase_date)
              AND YEAR(purchase_date) = YEAR(@purchase_date)
        );

        IF (@total_quantity + @quantity) > @quota
        BEGIN
            RAISERROR('Non-homeowners exceeded monthly quota for this item.', 16, 1);
            RETURN;
        END

        -- Insert purchase for non-homeowner
        INSERT INTO purchase (purchase_id, home_id, item_id, quantity, purchase_date, buyer_type)
        VALUES (@purchase_id, NULL, @item_id, @quantity, @purchase_date, @buyer_type);
    END

    -- Homeowner logic
    ELSE IF @buyer_type = 'Homeowner'
    BEGIN
        IF @home_id IS NULL
        BEGIN
            RAISERROR('Home ID is required for homeowners.', 16, 1);
            RETURN;
        END

        -- Insert purchase for homeowner
        INSERT INTO purchase (purchase_id, home_id, item_id, quantity, purchase_date, buyer_type)
        VALUES (@purchase_id, @home_id, @item_id, @quantity, @purchase_date, @buyer_type);
    END

    -- Invalid buyer type
    ELSE
    BEGIN
        RAISERROR('Invalid buyer type. Must be Homeowner or Non-homeowner.', 16, 1);
        RETURN;
    END
END;

SELECT 
    p.purchase_id,
    i.item_name,
    p.quantity,
    p.buyer_type,
    h.home_owner_name,
    p.purchase_date
FROM 
    purchase p
LEFT JOIN 
    item i ON p.item_id = i.item_id
LEFT JOIN 
    home h ON p.home_id = h.home_id
ORDER BY 
    p.purchase_date DESC;

	SELECT 
    purchase_id,
    item_id,
    quantity,
    buyer_type,
    home_id,
    purchase_date
FROM 
    purchase
ORDER BY 
    purchase_date DESC;


