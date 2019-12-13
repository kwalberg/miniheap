public class MiniHeap {

    int[] heap;
    int heapSize;
    int topPtr;

    public static void main(String[] args) {
        MiniHeap testHeap = new MiniHeap();
        int ptr1 = testHeap.malloc(10);
        int ptr2 = testHeap.malloc(10);
        int ptr3 = testHeap.malloc(10);
        testHeap.free(ptr2);
        System.out.println(testHeap);
        testHeap.free(ptr3);
        System.out.println(testHeap);
    }

    /**
     * The MiniHeap class is a naive implementation of C-style heap
     * memory management using the linked-list metadata implementations
     * of malloc() and free(). A much more complex and fully-featured
     * version of this implementation is used by GLIBC and most (if not
     * all) other modern C library implementations.
     */
    public MiniHeap(){
        this.heap = new int[heapSize];
        this.heapSize = 256;
        this.topPtr = 0;
    }

    /**
     * Allocates the next available block on the heap.
     *
     * To find the next block, malloc() uses heap metadata. For
     * this implementation, metadata is stored in the first 4 "bytes"
     * of each heap block. The format for metadata is as follows:
     *  - meta[0]: The size of the current heap block
     *  - meta[1]: Flag indicating whether the current block is full (1 is full, 0 is empty)
     *  - meta[2]: The address index of the previous block
     *  - meta[3]: The address index of the next block
     *
     * @param size Size of the block to allocate
     */
    public int malloc(int size){

        // Establish a minimum block size of 4 (prevents excessive fragmentation and 0-size blocks)
        if( size < 4){
            size = 4;
        }

        int blockPtr = 0;
        int blockSize = heap[0];
        int isFull = heap[1];
        int prev = heap[2];
        int next = heap[3];

        // Find the next empty block with enough open space (there must be at least one)
        // This operation is lazy - it doesn't attempt to find the best fit, just the first open slot
        while( isFull == 1 || (blockSize != 0 && blockSize > size)){
            blockPtr = next;
            blockSize = heap[blockPtr];
            isFull = heap[blockPtr + 1];
            prev = heap[blockPtr + 2];
            next = heap[blockPtr + 3];

        }

        // Mark block as full
        heap[blockPtr + 1] = 1;

        if (blockPtr == topPtr){ // Generate a new free chunk if we are allocating the last chunk in the heap

            topPtr += (4 + size);
            heap[topPtr + 2] = blockPtr;
            heap[blockPtr + 3] = topPtr;

            //Update size of current block
            heap[blockPtr] = size;

        } else if ( size < (blockSize - 8)){ // Generate a new free chunk if there is sufficient space left in the current chunk

            int newBlockAddr = blockPtr + size + 4;

            heap[blockPtr + 3] = newBlockAddr;
            heap[next + 2] = newBlockAddr;

            // Create metadata for new chunk
            heap[newBlockAddr] = blockSize - size - 4;
            heap[newBlockAddr + 1] = 0;
            heap[newBlockAddr + 2] = blockPtr;
            heap[newBlockAddr + 3] = next;

            // Update size of current block
            heap[blockPtr] = size;

        }


        // Return a pointer to the start of the data segment of the allocated block
        return blockPtr + 4;

    }

    /**
     * Deallocates the block at *ptr
     *
     * free() first checks whether the previous block is full. If
     * not, the current block is merged with the previous block to
     * create a larger free block by editing the previous block's
     * metadata. If the previous block is in use, the current chunk
     * has it
     *
     * Note that free() does not check if the pointer passed to it
     * references a valid heap block before executing. Unintended
     * consequences may occur if the pointer passed to free()
     * is not a correct pointer to a currently allocated block.
     *
     * @param dataPtr The address of the data segment of the block to be deallocated
     */
    public void free(int dataPtr){

        // Update the pointer to point to metadata, not data
        int headerPtr = dataPtr - 4;

        int prevBlockData = heap[headerPtr + 2];
        if (heap[prevBlockData + 1] == 0){

            // Increase size of previous block and update "next" pointer
            heap[prevBlockData] += (heap[headerPtr] + 4);
            heap[prevBlockData + 3] = heap[headerPtr + 3];

            // Zero out current block metadata
            heap[headerPtr] = 0;
            heap[headerPtr + 1] = 0;
            heap[headerPtr + 2] = 0;
            heap[headerPtr + 3] = 0;

        } else {
            heap[headerPtr + 1] = 0;
        }
    }

    public String toString(){
        String heapStr = "";
        for(int i = 0; i < 256; i++){
            if( i % 16 == 0 ){
                heapStr += "\n";
            }
            heapStr += String.format("%02X ", this.heap[i]) + " ";
        }
        return heapStr;
    }
}
