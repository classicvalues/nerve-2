/**
 * MIT License
 * <p>
 * Copyright (c) 2019-2020 nerve.network
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package network.nerve.converter.utils;

import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.manager.RocksDBManager;
import io.nuls.core.rockdb.service.RocksDBService;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

import java.nio.charset.StandardCharsets;

/**
 * @author: Mimi
 * @date: 2019-02-28
 */
public class ConverterDBUtil {

    private static final RuntimeSchema MODEL_WRAPPER_SCHEMA = RuntimeSchema.createFrom(ModelWrapper.class);

    public static <T> boolean putModel(String area, byte[] key, T value) throws Exception {
        if (!baseCheckArea(area)) {
            return false;
        }
        if (key == null || value == null) {
            return false;
        }
        try {
            byte[] bytes = getModelSerialize(value);
            return RocksDBService.put(area, key, bytes);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
            throw e;
        }
    }

    private static boolean baseCheckArea(String areaName) {
        if (StringUtils.isBlank(areaName) || RocksDBManager.getTable(areaName) == null) {
            return false;
        }
        return true;
    }

    public static <T> T getModel(byte[] value, Class<T> clazz) {
        if (value == null) {
            return null;
        }
        try {
            ModelWrapper model = new ModelWrapper();
            ProtostuffIOUtil.mergeFrom(value, model, MODEL_WRAPPER_SCHEMA);
            if (clazz != null && model.getT() != null) {
                return clazz.cast(model.getT());
            }
            return (T) model.getT();
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
            return null;
        }
    }

    public static <T> byte[] getModelSerialize(T value) {
        ModelWrapper modelWrapper = new ModelWrapper(value);
        byte[] bytes = ProtostuffIOUtil.toByteArray(modelWrapper, MODEL_WRAPPER_SCHEMA, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        return bytes;
    }

    public static <T> T getModel(String area, byte[] key, Class<T> clazz) {
        if (!baseCheckArea(area)) {
            return null;
        }
        if (key == null) {
            return null;
        }
        try {
            byte[] bytes = RocksDBService.get(area, key);
            if (bytes == null) {
                return null;
            }
            ModelWrapper model = new ModelWrapper();
            ProtostuffIOUtil.mergeFrom(bytes, model, MODEL_WRAPPER_SCHEMA);
            if (clazz != null && model.getT() != null) {
                return clazz.cast(model.getT());
            }
            return (T) model.getT();
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
            return null;
        }
    }

    public static byte[] stringToBytes(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
