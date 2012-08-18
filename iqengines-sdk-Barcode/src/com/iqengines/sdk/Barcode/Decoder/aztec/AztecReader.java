/*
 * Copyright 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iqengines.sdk.Barcode.Decoder.aztec;

import com.iqengines.sdk.Barcode.Decoder.BarcodeFormat;
import com.iqengines.sdk.Barcode.Decoder.ChecksumException;
import com.iqengines.sdk.Barcode.Decoder.DecodeHintType;
import com.iqengines.sdk.Barcode.Decoder.FormatException;
import com.iqengines.sdk.Barcode.Decoder.NotFoundException;
import com.iqengines.sdk.Barcode.Decoder.Reader;
import com.iqengines.sdk.Barcode.Decoder.Result;
import com.iqengines.sdk.Barcode.BinaryBitmap;
import com.iqengines.sdk.Barcode.Decoder.aztec.AztecDetectorResult;
import com.iqengines.sdk.Barcode.Decoder.common.DecoderResult;
import com.iqengines.sdk.Barcode.Decoder.aztec.decoder.Decoder;
import com.iqengines.sdk.Barcode.Decoder.aztec.detector.Detector;
import com.iqengines.sdk.Barcode.Decoder.ResultMetadataType;
import com.iqengines.sdk.Barcode.Decoder.ResultPoint;
import com.iqengines.sdk.Barcode.Decoder.ResultPointCallback;

import java.util.List;
import java.util.Map;

/**
 * This implementation can detect and decode Aztec codes in an image.
 *
 * @author David Olivier
 */
public final class AztecReader implements Reader {

  /**
   * Locates and decodes a Data Matrix code in an image.
   *
   * @return a String representing the content encoded by the Data Matrix code
   * @throws NotFoundException if a Data Matrix code cannot be found
   * @throws FormatException if a Data Matrix code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
    return decode(image, null);
  }

  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints)
      throws NotFoundException, FormatException {

    AztecDetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect();
    ResultPoint[] points = detectorResult.getPoints();

    if (hints != null) {
      ResultPointCallback rpcb = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
      if (rpcb != null) {
        for (ResultPoint point : points) {
          rpcb.foundPossibleResultPoint(point);
        }
      }
    }

    DecoderResult decoderResult = new Decoder().decode(detectorResult);

    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.AZTEC);
    
    List<byte[]> byteSegments = decoderResult.getByteSegments();
    if (byteSegments != null) {
      result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
    }
    String ecLevel = decoderResult.getECLevel();
    if (ecLevel != null) {
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
    }
    
    return result;
  }

  @Override
  public void reset() {
    // do nothing
  }

}