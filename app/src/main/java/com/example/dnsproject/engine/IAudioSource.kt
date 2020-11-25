package com.example.dnsproject.engine

interface IAudioSource {
    /**
     * Implement that it need to prepare the voice source.
     *
     * @return true, if the source is available.
     */
    fun prepare(): Boolean

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     * Single channel
     *
     * @param buffer byte buffer to write the audio data.
     * @return the size of buffer.
     */
    fun read(buffer: ByteArray): Int

    /**
     * Read the audio data from source and write on the buffer given by parameter.
     * Multi-channel.
     *
     * @param buffers byte buffers to write the audio data.
     * @return the size of buffer.
     */
    fun read(buffers: Array<ByteArray>): Int

    /**
     * Returns channel count
     */
    fun getChannel(): Int

    /**
     * Release the resources. This is called once at the end.
     */
    fun release()
}