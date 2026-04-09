import psycopg2

# Your Render connection details
conn_string = "postgresql://tradejournal_6jrx_user:rIf7dc15HrRlzal1cxlg0EScLGOWbIF5@dpg-d77u49p4bi0s73f5ti5g-a.oregon-postgres.render.com/tradejournal_6jrx"

try:
    print("🔗 Connecting to Render PostgreSQL...")
    conn = psycopg2.connect(conn_string)
    cursor = conn.cursor()
    print("✅ Connection successful!")
    
    # Read and execute the SQL file
    print("\n📥 Loading demo data...")
    with open('demo_data_postgres.sql', 'r') as f:
        sql = f.read()
    
    # Split SQL into individual statements
    statements = [s.strip() for s in sql.split(';') if s.strip()]
    
    for i, statement in enumerate(statements, 1):
        try:
            cursor.execute(statement)
            print(f"✅ Statement {i}/{len(statements)} executed")
        except Exception as e:
            if "duplicate" in str(e).lower():
                print(f"⚠️  Statement {i}: Data already exists (skipped)")
            else:
                print(f"❌ Statement {i} error: {e}")
    
    conn.commit()
    print("\n✅ Demo data loaded successfully!")
    
    # Verify
    cursor.execute('SELECT COUNT(*) FROM "Trade"')
    trade_count = cursor.fetchone()[0]
    print(f"\n📊 Verification: {trade_count} trades in database")
    
    cursor.close()
    conn.close()
    
except Exception as e:
    print(f"❌ Connection error: {e}")
    print("\n💡 Make sure:")
    print("  1. PostgreSQL client libraries are installed")
    print("  2. The connection string is correct")
    print("  3. Your Render database is accessible")
