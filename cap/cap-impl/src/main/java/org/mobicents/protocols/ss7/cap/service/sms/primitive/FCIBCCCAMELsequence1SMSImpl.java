/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.protocols.ss7.cap.service.sms.primitive;

import java.io.IOException;

import org.mobicents.protocols.asn.AsnException;
import org.mobicents.protocols.asn.AsnInputStream;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.asn.Tag;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.CAPParsingComponentException;
import org.mobicents.protocols.ss7.cap.api.CAPParsingComponentExceptionReason;
import org.mobicents.protocols.ss7.cap.api.primitives.AppendFreeFormatData;
import org.mobicents.protocols.ss7.cap.api.service.sms.primitive.FCIBCCCAMELsequence1SMS;
import org.mobicents.protocols.ss7.cap.api.service.sms.primitive.FreeFormatDataSMS;
import org.mobicents.protocols.ss7.cap.primitives.SequenceBase;
import org.mobicents.protocols.ss7.inap.api.INAPParsingComponentException;
import org.mobicents.protocols.ss7.map.api.MAPParsingComponentException;

/**
 *
 * @author Lasith Waruna Perera
 *
 */
public class FCIBCCCAMELsequence1SMSImpl extends SequenceBase implements FCIBCCCAMELsequence1SMS {

    public static final int _ID_freeFormatData = 0;
    public static final int _ID_appendFreeFormatData = 1;

    public static final int _ID_FCIBCCCAMELsequence1 = 0;

    private FreeFormatDataSMS freeFormatData;
    private AppendFreeFormatData appendFreeFormatData;

    public FCIBCCCAMELsequence1SMSImpl() {
        super("FCIBCCCAMELsequence1SMS");
    }

    public FCIBCCCAMELsequence1SMSImpl(FreeFormatDataSMS freeFormatData, AppendFreeFormatData appendFreeFormatData) {
        super("FCIBCCCAMELsequence1SMS");
        this.freeFormatData = freeFormatData;
        this.appendFreeFormatData = appendFreeFormatData;
    }

    @Override
    public FreeFormatDataSMS getFreeFormatData() {
        return this.freeFormatData;
    }

    @Override
    public AppendFreeFormatData getAppendFreeFormatData() {
        return this.appendFreeFormatData;
    }

    @Override
    public int getTag() throws CAPException {
        return _ID_FCIBCCCAMELsequence1;
    }

    @Override
    public int getTagClass() {
        return Tag.CLASS_CONTEXT_SPECIFIC;
    }

    @Override
    protected void _decode(AsnInputStream asnIS, int length) throws CAPParsingComponentException, IOException,
            AsnException, MAPParsingComponentException, INAPParsingComponentException {

        this.freeFormatData = null;
        this.appendFreeFormatData = AppendFreeFormatData.overwrite;

        AsnInputStream ais = asnIS.readSequenceStreamData(length);
        while (true) {
            if (ais.available() == 0)
                break;

            int tag = ais.readTag();

            if (ais.getTagClass() == Tag.CLASS_CONTEXT_SPECIFIC) {
                switch (tag) {
                case _ID_freeFormatData:
                    if (!ais.isTagPrimitive())
                        throw new CAPParsingComponentException("Error while decoding " + _PrimitiveName
                                + ".freeFormatData: Parameter is not primitive",
                                CAPParsingComponentExceptionReason.MistypedParameter);
                    this.freeFormatData = new FreeFormatDataSMSImpl();
                    ((FreeFormatDataSMSImpl) this.freeFormatData).decodeAll(ais);
                    break;
                case _ID_appendFreeFormatData:
                    if (!ais.isTagPrimitive())
                        throw new CAPParsingComponentException("Error while decoding " + _PrimitiveName
                                + ".appendFreeFormatData: Parameter is not primitive",
                                CAPParsingComponentExceptionReason.MistypedParameter);
                    int i1 = (int) ais.readInteger();
                    this.appendFreeFormatData = AppendFreeFormatData.getInstance(i1);
                    break;
                default:
                    ais.advanceElement();
                    break;
                }
            } else {
                ais.advanceElement();
            }
        }

        if (this.freeFormatData == null)
            throw new CAPParsingComponentException("Error while decoding " + _PrimitiveName
                    + ": parameter freeFormatData is mandatory but not found",
                    CAPParsingComponentExceptionReason.MistypedParameter);

    }

    @Override
    public void encodeData(AsnOutputStream asnOs) throws CAPException {

        if (this.freeFormatData == null)
            throw new CAPException("Error while encoding " + _PrimitiveName + ": freeFormatData must not be null");

        try {

            ((FreeFormatDataSMSImpl) this.freeFormatData).encodeAll(asnOs, Tag.CLASS_CONTEXT_SPECIFIC, _ID_freeFormatData);

            if (this.appendFreeFormatData != null) {
                asnOs.writeInteger(Tag.CLASS_CONTEXT_SPECIFIC, _ID_appendFreeFormatData,
                        this.appendFreeFormatData.getCode());
            }

        } catch (AsnException e) {
            throw new CAPException("AsnException when encoding " + _PrimitiveName + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new CAPException("AsnException when encoding " + _PrimitiveName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(_PrimitiveName);
        sb.append(" [");

        if (this.freeFormatData != null) {
            sb.append("freeFormatData=");
            sb.append(freeFormatData.toString());
        }
        if (this.appendFreeFormatData != null) {
            sb.append(", appendFreeFormatData=");
            sb.append(appendFreeFormatData.toString());
        }

        sb.append("]");

        return sb.toString();
    }

}
