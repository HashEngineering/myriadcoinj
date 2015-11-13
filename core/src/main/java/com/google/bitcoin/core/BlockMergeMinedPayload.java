package com.google.bitcoin.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jagdeep.sidhu on 1/5/14.
 */
public class BlockMergeMinedPayload  {
    private static final Logger log = LoggerFactory.getLogger(BlockMergeMinedPayload.class);
    public transient int cursor;
    transient NetworkParameters params;
    // Merged mining fields
    //Parent Block TX
    public transient Transaction parentBlockCoinBaseTx;
    public transient Block block;
    private transient  byte bytes[];
    public transient int length;
    //Coinbase Link
    public transient Sha256Hash hashOfParentBlockHeader;

    //Parent Block Header
    public transient Block parentBlockHeader;
    private transient boolean parsed;
    public BlockMergeMinedPayload(NetworkParameters parameters, byte[] payloadBytes, int cursorStart, Block block) throws ProtocolException
    {
        parsed = false;
        bytes = payloadBytes;
        this.block = block;
        this.params = parameters;
        if(bytes != null)
            parse(cursorStart);
        bytes = null;

    }
    void parse(int cursorStart) throws ProtocolException
    {
        length = 0;
        parsed = false;
        cursor = cursorStart;
        parseMergedMineInfo();
        length = cursor-cursorStart;
        cursor = cursorStart;
        if(length > 0)
        {
            parsed = true;
        }

    }
    public boolean IsValid()
    {
        return parsed;
    }
    private void parseMergedMineInfo() throws ProtocolException
    {

        if(bytes == null || bytes.length <= (Block.HEADER_SIZE+1))
        {
            log.info("Warning: Trying to parse merged-mine info from information passed in that doesn't include merged-mine information, skipping...");
            return;
        }
        // Parent Block Coinbase Transaction:
        parentBlockCoinBaseTx = new Transaction(params, bytes, cursor, this.block, false, false, Block.UNKNOWN_LENGTH);
        parentBlockCoinBaseTx.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
        cursor += parentBlockCoinBaseTx.getMessageSize();

        // Coinbase Link:
        // Hash of parent block header
        hashOfParentBlockHeader = readHash();

        // Number of links in branch

        long numHashes = readVarInt();

        // Hash #1 - #numHashes
        cursor += 32*numHashes;

        // Branch sides bitmask

        cursor += 4;

        // Aux Blockchain Link:
        // Number of links in branch
        numHashes = readVarInt();
        // Hash #1 - #numHashes
        cursor += 32*numHashes;

        // Branch sides bitmask
        cursor += 4;
        byte[] header = readBytes(80);
        // Parent Block Header:
        parentBlockHeader = new Block(this.params, null, header, false, false, 80, 0);
        // reads in the block information as needed
        Sha256Hash hashOfParentBlockHeaderCalculated =   parentBlockHeader.getHash(); //calculateHash();
        String str = parentBlockCoinBaseTx.toString();
        String txHash = parentBlockCoinBaseTx.getHashAsString();
        /*Note that the block_hash element is not needed as you have the full parent_block header element and can calculate the hash from that. The current Namecoin client doesn't check this field for validity, and as such some AuxPOW blocks have it little-endian, and some have it big-endian. */
        /*https://en.bitcoin.it/wiki/Merged_mining_specification*/
        if(!hashOfParentBlockHeader.equals(hashOfParentBlockHeaderCalculated))
        {
            Sha256Hash reversedHashOfParentBlockHeader =  new Sha256Hash(Utils.reverseBytes(hashOfParentBlockHeader.getBytes()));
            if(!reversedHashOfParentBlockHeader.equals(hashOfParentBlockHeaderCalculated)){
                //throw new ProtocolException("Hash of parent block header calculated does not match hash of parent block header received in merged-mining header.");
                //Namecoin doesn't check this field
            }
            else
            {
                hashOfParentBlockHeader = reversedHashOfParentBlockHeader;
            }
        }
    }

    /**
     * Returns a multi-line string containing a description of the contents of
     * the block. Use for debugging purposes only.
     */
    long readVarInt() throws ProtocolException {
        return readVarInt(0);
    }
    long readVarInt(int offset) throws ProtocolException {
        try {
            VarInt varint = new VarInt(bytes, cursor + offset);
            cursor += offset + varint.getOriginalSizeInBytes();
            return varint.value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
    public String toString() {
        StringBuilder s = new StringBuilder("");
        s.append("      parent block coin base transaction: \n");
        s.append(parentBlockCoinBaseTx.toString());
        s.append("\n");
        if(hashOfParentBlockHeader != null)
        {
            s.append("      coinbase link: \n");
            s.append("          hash of parent block header: ");
            s.append(hashOfParentBlockHeader);
            s.append("\n");
        }
        if(parentBlockHeader != null)
        {
            s.append("      parent block header: \n");
            s.append(parentBlockHeader.toString());
            s.append("\n");
        }
        return s.toString();
    }
    byte[] readBytes(int length) throws ProtocolException {
        try {
            byte[] b = new byte[length];
            System.arraycopy(bytes, cursor, b, 0, length);
            cursor += length;
            return b;
        } catch (IndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
    long readInt64() throws ProtocolException {
        try {
            long u = Utils.readInt64(bytes, cursor);
            cursor += 8;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
    long readUint32() throws ProtocolException {
        try {
            long u = Utils.readUint32(bytes, cursor);
            cursor += 4;
            return u;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }

    Sha256Hash readHash() throws ProtocolException {
        try {
            byte[] hash = new byte[32];
            System.arraycopy(bytes, cursor, hash, 0, 32);
            // We have to flip it around, as it's been read off the wire in little endian.
            // Not the most efficient way to do this but the clearest.
            hash = Utils.reverseBytes(hash);
            cursor += 32;
            return new Sha256Hash(hash);
        } catch (IndexOutOfBoundsException e) {
            throw new ProtocolException(e);
        }
    }
}
