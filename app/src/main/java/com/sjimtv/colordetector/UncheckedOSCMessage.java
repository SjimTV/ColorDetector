/*
 * Copyright (C) 2003-2014, C. Ramakrishnan / Illposed Software.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.sjimtv.colordetector;

import com.illposed.osc.OSCPacket;
import com.illposed.osc.utility.OSCJavaToByteArrayConverter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An simple (non-bundle) OSC message.
 *
 * An OSC <i>Message</i> is made up of
 * an <i>Address Pattern</i> (the receiver of the message)
 * and <i>Arguments</i> (the content of the message).
 *
 * @author Chandrasekhar Ramakrishnan
 */
public class UncheckedOSCMessage extends UncheckedAbstractOSCPacket {

    /**
     * Java regular expression pattern matching a single invalid character.
     * The invalid characters are:
     * ' ', '#', '*', ',', '?', '[', ']', '{', '}'
     */
    private static final Pattern ILLEGAL_ADDRESS_CHAR
            = Pattern.compile("[ \\#\\*\\,\\?\\[\\]\\{\\}]");

    private String address;
    private List<Object> arguments;

    /**
     * Creates an empty OSC Message.
     * In order to send this OSC message,
     * you need to set the address and optionally some arguments.
     */
    public UncheckedOSCMessage() {
        this(null);
    }

    /**
     * Creates an OSCMessage with an address already initialized.
     * @param address  the recipient of this OSC message
     */
    public UncheckedOSCMessage(String address) {
        this(address, null);
    }

    /**
     * Creates an OSCMessage with an address
     * and arguments already initialized.
     * @param address  the recipient of this OSC message
     * @param arguments  the data sent to the receiver
     */
    public UncheckedOSCMessage(String address, Collection<Object> arguments) {

        //checkAddress(address);
        this.address = address;
        if (arguments == null) {
            this.arguments = new LinkedList<Object>();
        } else {
            this.arguments = new ArrayList<Object>(arguments);
        }
        init();
    }

    /**
     * The receiver of this message.
     * @return the receiver of this OSC Message
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the address of this message.
     * @param address the receiver of the message
     */
    public void setAddress(String address) {
        checkAddress(address);
        this.address = address;
        contentChanged();
    }

    /**
     * Add an argument to the list of arguments.
     * @param argument a Float, Double, String, Character, Integer, Long, Boolean, null
     *   or an array of these
     */
    public void addArgument(Object argument) {
        arguments.add(argument);
        contentChanged();
    }

    /**
     * The arguments of this message.
     * @return the arguments to this message
     */
    public List<Object> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * Convert the address into a byte array.
     * Used internally only.
     * @param stream where to write the address to
     */
    private void computeAddressByteArray(OSCJavaToByteArrayConverter stream) {
        stream.write(address);
    }

    /**
     * Convert the arguments into a byte array.
     * Used internally only.
     * @param stream where to write the arguments to
     */
    private void computeArgumentsByteArray(OSCJavaToByteArrayConverter stream) {
        stream.write(',');
        stream.writeTypes(arguments);
        for (Object argument : arguments) {
            stream.write(argument);
        }
    }

    @Override
    byte[] computeByteArray(OSCJavaToByteArrayConverter stream) {
        computeAddressByteArray(stream);
        computeArgumentsByteArray(stream);
        return stream.toByteArray();
    }

    /**
     * Throws an exception if the given address is invalid.
     * We explicitly allow <code>null</code> here,
     * because we want to allow to set the address in a lazy fashion.
     * @param address to be checked for validity
     */
    private static void checkAddress(String address) {
        // NOTE We explicitly allow <code>null</code> here,
        //   because we want to allow to set in a lazy fashion.
        if ((address != null) && !isValidAddress(address)) {
            throw new IllegalArgumentException("Not a valid OSC address: "
                    + address);
        }
    }

    /**
     * Checks whether a given string is a valid OSC <i>Address Pattern</i>.
     * @param address to be checked for validity
     * @return true if the supplied string constitutes a valid OSC address
     */
    public static boolean isValidAddress(String address) {
        return (address != null)
                && address.startsWith("/")
                && !address.contains("//")
                && !ILLEGAL_ADDRESS_CHAR.matcher(address).find();
    }

}

/**
 * OSCPacket is the abstract superclass for the various
 * kinds of OSC Messages.
 *
 * The actual packets are:
 * <ul>
 *   and/or made up of multiple messages
 * </ul>
 */
abstract class UncheckedAbstractOSCPacket implements OSCPacket {

    /** Used to encode message addresses and string parameters. */
    private Charset charset;
    private byte[] byteArray;

    public UncheckedAbstractOSCPacket() {
        this.charset = Charset.defaultCharset();
        this.byteArray = null;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Generate a representation of this packet conforming to the
     * the OSC byte stream specification. Used Internally.
     */
    private byte[] computeByteArray() {
        OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
        stream.setCharset(charset);
        return computeByteArray(stream);
    }

    /**
     * Produces a byte array representation of this packet.
     * @param stream where to write the arguments to
     * @return the OSC specification conform byte array representation
     *   of this packet
     */
    abstract byte[] computeByteArray(OSCJavaToByteArrayConverter stream);

    @Override
    public byte[] getByteArray() {
        if (byteArray == null) {
            byteArray = computeByteArray();
        }
        return byteArray;
    }

    protected void contentChanged() {
        byteArray = null;
    }

    /**
     * Run any post construction initialization. (By default, do nothing.)
     */
    protected void init() {

    }
}
