package com.google.bitcoin.core;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: HashEngineering
 * Date: 8/13/13
 * Time: 7:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoinDefinition {


    public static final String coinName = "Myriadcoin";
    public static final String coinTicker = "MYR";
    public static final String coinURIScheme = "myriadcoin";
    public static final String cryptsyMarketId = "200";
    public static final String cryptsyMarketCurrency = "BTC";
    public static final String PATTERN_PRIVATE_KEY_START = "6";

    public enum CoinPrecision {
        Coins,
        Millicoins,
    }
    public static final CoinPrecision coinPrecision = CoinPrecision.Coins;


    public static final String BLOCKEXPLORER_BASE_URL_PROD = "http://explorerino.com/";    //blockr.io
    public static final String BLOCKEXPLORER_ADDRESS_PATH = "address/";             //blockr.io path
    public static final String BLOCKEXPLORER_TRANSACTION_PATH = "tx/";              //blockr.io path
    public static final String BLOCKEXPLORER_BLOCK_PATH = "block/";                 //blockr.io path
    public static final String BLOCKEXPLORER_BASE_URL_TEST = BLOCKEXPLORER_BASE_URL_PROD;

    public static final String DONATION_ADDRESS = "MGjuxMt4XpPiX5a6TjVvSFdP83BnapkQFY";  //HashEngineering donation DGC address

    enum CoinHash {
        SHA256,
        scrypt,
        x11,
        custom
    };
    public static final CoinHash coinPOWHash = CoinHash.custom;

    public static boolean checkpointFileSupport = true;

    public static final int TARGET_TIMESPAN = (int)(150);  // 2.5 hours per difficulty cycle, on average.
    public static final int TARGET_SPACING = (int)(150);  // 2.5 minutes seconds per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;  //1 blocks

    static final long nAveragingInterval = 10; // 10 blocks
    static final long nAveragingTargetTimespan = nAveragingInterval * TARGET_SPACING; // 25 minutes

    static final long nMaxAdjustDown = 4; // 4% adjustment down
    static final long nMaxAdjustUp = 2; // 2% adjustment up

    static final long nMinActualTimespan = nAveragingTargetTimespan * (100 - nMaxAdjustUp) / 100;
    static final long nMaxActualTimespan = nAveragingTargetTimespan * (100 + nMaxAdjustDown) / 100;

    public static final int getInterval(int height, boolean testNet) {
            return INTERVAL;      //108
    }
    public static final int getIntervalCheckpoints() {
            return 2000;

    }
    public static final int getTargetTimespan(int height, boolean testNet) {
            return TARGET_TIMESPAN;    //72 min
    }
    public static BigInteger getProofOfWorkLimit(int algo)
    {
       return proofOfWorkLimits[algo];
    }

    public static int spendableCoinbaseDepth = 100; //main.h: static const int COINBASE_MATURITY
    public static final BigInteger MAX_MONEY = BigInteger.valueOf(2000000000).multiply(Utils.COIN);                 //main.h:  MAX_MONEY
    //public static final String MAX_MONEY_STRING = "200000000";     //main.h:  MAX_MONEY

    public static final BigInteger DEFAULT_MIN_TX_FEE = BigInteger.valueOf(100000);   // MIN_TX_FEE
    public static final BigInteger DUST_LIMIT = BigInteger.valueOf(1000); //main.h CTransaction::GetMinFee        0.01 coins

    public static final int PROTOCOL_VERSION = 70001;          //version.h PROTOCOL_VERSION
    public static final int MIN_PROTOCOL_VERSION = 209;        //version.h MIN_PROTO_VERSION
    public static final int BIP0031_VERSION = 60000;

    public static final int BLOCK_CURRENTVERSION = 2;   //CBlock::CURRENT_VERSION
    public static final int MAX_BLOCK_SIZE = 1 * 1000 * 1000;


    public static final boolean supportsBloomFiltering = false; //Requires PROTOCOL_VERSION 70000 in the client

    public static final int Port    = 10888;       //protocol.h GetDefaultPort(testnet=false)
    public static final int TestPort = 20888;     //protocol.h GetDefaultPort(testnet=true)

    //
    //  Production
    //
    public static final int AddressHeader = 50;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS
    public static final int p2shHeader = 9;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS
    public static final boolean allowBitcoinPrivateKey = false; //for backward compatibility with previous version of digitalcoin
    public static final int dumpedPrivateKeyHeader = 128;   //common to all coins
    public static final long PacketMagic = 0xaf4576ee;      //0xfb, 0xc0, 0xb6, 0xdb

    //Genesis Block Information from main.cpp: LoadBlockIndex
    static public long genesisBlockDifficultyTarget = (0x1e0fffffL);         //main.cpp: LoadBlockIndex
    static public long genesisBlockTime = 1393164995L;                       //main.cpp: LoadBlockIndex
    static public long genesisBlockNonce = (2092903596);                         //main.cpp: LoadBlockIndex
    static public String genesisHash = "00000ffde4c020b5938441a0ea3d314bf619eff0b38f32f78f7583cffa1ea485"; //main.cpp: hashGenesisBlock
    static public String genesisMerkleRoot = "3f75db3c18e92f46c21530dc1222e1fddf4ccebbf88e289a6c9dc787fd6469da";
    static public int genesisBlockValue = 1000;                                                              //main.cpp: LoadBlockIndex
    //taken from the raw data of the block explorer
    static public String genesisTxInBytes = "04ffff001d010436323031342d30322d3233204654202d204732302061696d7320746f20616464202432746e20746f20676c6f62616c2065636f6e6f6d79";   //"limecoin se convertira en una de las monedas mas segura del mercado, checa nuestros avances"
    static public String genesisTxOutBytes = "04e941763c7750969e751bee1ffbe96a651a0feb131db046546c219ea40bff40b95077dc9ba1c05af991588772d8daabbda57386c068fb9bc7477c5e28702d5eb9";

    //net.cpp strDNSSeed
    static public String[] dnsSeeds = new String[] {
            "seed1.myriadcoin.org",
            "seed2.myriadcoin.org",
            "seed3.myriadcoin.org",
            "seed4.myriadcoin.org",
            "85.195.118.163",
            "theblockexplorer.com"
    };

    public static int minBroadcastConnections = 1;   //0 for default; we need more peers.

    //
    // TestNet - not tested
    //
    public static final boolean supportsTestNet = false;
    public static final int testnetAddressHeader = 88;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS_TEST
    public static final int testnetp2shHeader = 188;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS_TEST
    public static final long testnetPacketMagic = 0x01f555a4;      //0xfc, 0xc1, 0xb7, 0xdc
    public static final String testnetGenesisHash = "0000017ce2a79c8bddafbbe47c004aa92b20678c354b34085f62b762084b9788";
    static public long testnetGenesisBlockDifficultyTarget = (0x1e0ffff0L);         //main.cpp: LoadBlockIndex
    static public long testnetGenesisBlockTime = 1392876393L;                       //main.cpp: LoadBlockIndex
    static public long testnetGenesisBlockNonce = (416875379);                         //main.cpp: LoadBlockIndex





    //main.cpp GetBlockValue(height, fee)
    static final BigInteger startSubsidy = BigInteger.valueOf(1000).multiply(Utils.COIN);
    static final BigInteger minSubsidy = Utils.COIN;

    public static final BigInteger GetBlockReward(int height)
    {
        BigInteger subsidy = startSubsidy;
        subsidy = subsidy.shiftRight(height / subsidyDecreaseBlockCount);

        if(subsidy.compareTo(minSubsidy) < 0)
            subsidy = minSubsidy;

        return subsidy;
    }

    public static int subsidyDecreaseBlockCount = 80640 * 12;     //main.cpp GetBlockValue(height, fee)

    public static BigInteger proofOfWorkLimit = Utils.decodeCompactBits(0x1e0fffffL);  //main.cpp bnProofOfWorkLimit (~uint256(0) >> 20); // digitalcoin: starting difficulty is 1 / 2^12
    public static BigInteger [] proofOfWorkLimits = new BigInteger[] {
        proofOfWorkLimit,proofOfWorkLimit,proofOfWorkLimit,proofOfWorkLimit,proofOfWorkLimit };

    /*proofOfWorkLimits[Block.ALGO_SHA256D] = proofOfWorkLimit;
    proofOfWorkLimits[Block.ALGO_SCRYPT]  = proofOfWorkLimit;
    proofOfWorkLimits[ALGO_GROESTL] = proofOfWorkLimit;
    proofOfWorkLimits[ALGO_SKEIN]   = proofOfWorkLimit;
    proofOfWorkLimits[ALGO_QUBIT]   = proofOfWorkLimit;*/



    static public String[] testnetDnsSeeds = new String[] {
            "testseed1.myriadcoin.org"
    };
    //from main.h: CAlert::CheckSignature
    public static final String SATOSHI_KEY = "04a82e43bebee0af77bb6d4f830c5b2095b7479a480e91bbbf3547fb261c5e6d1be2c27e3c57503f501480f5027371ec62b2be1b6f00fc746e4b3777259e7f6a78";
    public static final String TESTNET_SATOSHI_KEY = "044adf046e6bc86fb83ef92f261fa3feff9176bd029c5ad4afb5c52ac21f9851f2b2eb861cdbf2c09e0cb97dbf75c6ca5ff6c5df88cfb244c72dba1d44b5a47655";

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.myriadcoin.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = "org.myriadcoin.test";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "com.google.myriadcoin.unittest";

    //checkpoints.cpp Checkpoints::mapCheckpoints
    public static void initCheckpoints(Map<Integer, Sha256Hash> checkpoints)
    {

        checkpoints.put(     0, new Sha256Hash("00000ffde4c020b5938441a0ea3d314bf619eff0b38f32f78f7583cffa1ea485"));
        checkpoints.put(  9646, new Sha256Hash("0000000000000b0f1372211861f226a3ec06a27d0a5bf36e4244a982da077e8f"));
        checkpoints.put( 27255, new Sha256Hash("00000000000005112a0debf53703eb3dc4ec2d8d68599c90db71423ea14489b7"));
        checkpoints.put( 70623, new Sha256Hash("00000000000004767ff6e509d00772af5c4bedaa82c38c1e95c33adbf5ff84f5"));
        checkpoints.put(112567, new Sha256Hash("000000000000018c0621bf32ab33d3ca871509f406f08be6dd20facea747b099"));
        checkpoints.put(141845, new Sha256Hash("00000000000000f62d14d55c2bc3ec0ba94e4f2b3868bbe7be9cb5b681fcc0fb"));
        checkpoints.put(149540, new Sha256Hash("000000000000061b8f5b8653fe13b5e453347d9386d43d09445ee5e26a8222bb"));

    }

    //Unit Test Information
    public static final String UNITTEST_ADDRESS = "";
    public static final String UNITTEST_ADDRESS_PRIVATE_KEY = "";

}
