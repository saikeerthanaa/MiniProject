const { Client } = require('pg');
const fs = require('fs');

const connectionString = 'postgresql://tradejournal_6jrx_user:rIf7dc15HrRlzal1cxlg0EScLGOWbIF5@dpg-d77u49p4bi0s73f5ti5g-a.oregon-postgres.render.com/tradejournal_6jrx';

const client = new Client({
  connectionString: connectionString,
  ssl: { rejectUnauthorized: false }
});

async function loadData() {
  try {
    console.log('🔗 Connecting to Render PostgreSQL...');
    await client.connect();
    console.log('✅ Connection successful!');

    // Read SQL file
    console.log('\n📥 Loading demo data...');
    const sql = fs.readFileSync('demo_data_postgres.sql', 'utf8');
    
    // Execute SQL
    await client.query(sql);
    console.log('✅ Demo data loaded successfully!');

    // Verify
    const result = await client.query('SELECT COUNT(*) FROM "Trade"');
    console.log(`\n📊 Verification: ${result.rows[0].count} trades in database`);

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await client.end();
  }
}

loadData();
