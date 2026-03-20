package kotlin;

import kotlin.jvm.JvmInline;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.UIntRange;
import kotlin.ranges.URangesKt;

@JvmInline
@Metadata(d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u000f\n\u0000\n\u0002\u0010\n\n\u0002\b\t\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\u0010\u0000\n\u0002\b!\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0005\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u000e\b@\u0018\u0000 v2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001vB\u0014\b\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003ø\u0001\u0000¢\u0006\u0004\b\u0004\u0010\u0005J\u001b\u0010\b\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\fø\u0001\u0000¢\u0006\u0004\b\n\u0010\u000bJ\u001b\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u000eH\nø\u0001\u0000¢\u0006\u0004\b\u000f\u0010\u0010J\u001b\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u0011H\nø\u0001\u0000¢\u0006\u0004\b\u0012\u0010\u0013J\u001b\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u0014H\nø\u0001\u0000¢\u0006\u0004\b\u0015\u0010\u0016J\u001b\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\b\u0017\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u0000H\nø\u0001\u0001ø\u0001\u0000¢\u0006\u0004\b\u001a\u0010\u0005J\u001b\u0010\u001b\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\nø\u0001\u0000¢\u0006\u0004\b\u001c\u0010\u0010J\u001b\u0010\u001b\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\nø\u0001\u0000¢\u0006\u0004\b\u001d\u0010\u0013J\u001b\u0010\u001b\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\nø\u0001\u0000¢\u0006\u0004\b\u001e\u0010\u001fJ\u001b\u0010\u001b\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\b \u0010\u0018J\u001a\u0010!\u001a\u00020\"2\b\u0010\t\u001a\u0004\u0018\u00010#HÖ\u0003¢\u0006\u0004\b$\u0010%J\u001b\u0010&\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\bø\u0001\u0000¢\u0006\u0004\b'\u0010\u0010J\u001b\u0010&\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\bø\u0001\u0000¢\u0006\u0004\b(\u0010\u0013J\u001b\u0010&\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\bø\u0001\u0000¢\u0006\u0004\b)\u0010\u001fJ\u001b\u0010&\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\bø\u0001\u0000¢\u0006\u0004\b*\u0010\u0018J\u0010\u0010+\u001a\u00020\rHÖ\u0001¢\u0006\u0004\b,\u0010-J\u0016\u0010.\u001a\u00020\u0000H\nø\u0001\u0001ø\u0001\u0000¢\u0006\u0004\b/\u0010\u0005J\u0016\u00100\u001a\u00020\u0000H\bø\u0001\u0001ø\u0001\u0000¢\u0006\u0004\b1\u0010\u0005J\u001b\u00102\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\nø\u0001\u0000¢\u0006\u0004\b3\u0010\u0010J\u001b\u00102\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\nø\u0001\u0000¢\u0006\u0004\b4\u0010\u0013J\u001b\u00102\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\nø\u0001\u0000¢\u0006\u0004\b5\u0010\u001fJ\u001b\u00102\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\b6\u0010\u0018J\u001b\u00107\u001a\u00020\u000e2\u0006\u0010\t\u001a\u00020\u000eH\bø\u0001\u0000¢\u0006\u0004\b8\u00109J\u001b\u00107\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\bø\u0001\u0000¢\u0006\u0004\b:\u0010\u0013J\u001b\u00107\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\bø\u0001\u0000¢\u0006\u0004\b;\u0010\u001fJ\u001b\u00107\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\bø\u0001\u0000¢\u0006\u0004\b<\u0010\u000bJ\u001b\u0010=\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\fø\u0001\u0000¢\u0006\u0004\b>\u0010\u000bJ\u001b\u0010?\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\nø\u0001\u0000¢\u0006\u0004\b@\u0010\u0010J\u001b\u0010?\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\nø\u0001\u0000¢\u0006\u0004\bA\u0010\u0013J\u001b\u0010?\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\nø\u0001\u0000¢\u0006\u0004\bB\u0010\u001fJ\u001b\u0010?\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\bC\u0010\u0018J\u001b\u0010D\u001a\u00020E2\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\bF\u0010GJ\u001b\u0010H\u001a\u00020E2\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\bI\u0010GJ\u001b\u0010J\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\nø\u0001\u0000¢\u0006\u0004\bK\u0010\u0010J\u001b\u0010J\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\nø\u0001\u0000¢\u0006\u0004\bL\u0010\u0013J\u001b\u0010J\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\nø\u0001\u0000¢\u0006\u0004\bM\u0010\u001fJ\u001b\u0010J\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\bN\u0010\u0018J\u001b\u0010O\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u000eH\nø\u0001\u0000¢\u0006\u0004\bP\u0010\u0010J\u001b\u0010O\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0011H\nø\u0001\u0000¢\u0006\u0004\bQ\u0010\u0013J\u001b\u0010O\u001a\u00020\u00142\u0006\u0010\t\u001a\u00020\u0014H\nø\u0001\u0000¢\u0006\u0004\bR\u0010\u001fJ\u001b\u0010O\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u0000H\nø\u0001\u0000¢\u0006\u0004\bS\u0010\u0018J\u0010\u0010T\u001a\u00020UH\b¢\u0006\u0004\bV\u0010WJ\u0010\u0010X\u001a\u00020YH\b¢\u0006\u0004\bZ\u0010[J\u0010\u0010\\\u001a\u00020]H\b¢\u0006\u0004\b^\u0010_J\u0010\u0010`\u001a\u00020\rH\b¢\u0006\u0004\ba\u0010-J\u0010\u0010b\u001a\u00020cH\b¢\u0006\u0004\bd\u0010eJ\u0010\u0010f\u001a\u00020\u0003H\b¢\u0006\u0004\bg\u0010\u0005J\u000f\u0010h\u001a\u00020iH\u0016¢\u0006\u0004\bj\u0010kJ\u0016\u0010l\u001a\u00020\u000eH\bø\u0001\u0001ø\u0001\u0000¢\u0006\u0004\bm\u0010WJ\u0016\u0010n\u001a\u00020\u0011H\bø\u0001\u0001ø\u0001\u0000¢\u0006\u0004\bo\u0010-J\u0016\u0010p\u001a\u00020\u0014H\bø\u0001\u0001ø\u0001\u0000¢\u0006\u0004\bq\u0010eJ\u0016\u0010r\u001a\u00020\u0000H\bø\u0001\u0001ø\u0001\u0000¢\u0006\u0004\bs\u0010\u0005J\u001b\u0010t\u001a\u00020\u00002\u0006\u0010\t\u001a\u00020\u0000H\fø\u0001\u0000¢\u0006\u0004\bu\u0010\u000bR\u0016\u0010\u0002\u001a\u00020\u00038\u0000X\u0004¢\u0006\b\n\u0000\u0012\u0004\b\u0006\u0010\u0007\u0001\u0002\u0001\u00020\u0003ø\u0001\u0000\u0002\b\n\u0002\b\u0019\n\u0002\b!¨\u0006w"}, d2 = {"Lkotlin/UShort;", "", "data", "", "constructor-impl", "(S)S", "getData$annotations", "()V", "and", "other", "and-xj2QHRw", "(SS)S", "compareTo", "", "Lkotlin/UByte;", "compareTo-7apg3OU", "(SB)I", "Lkotlin/UInt;", "compareTo-WZ4Q5Ns", "(SI)I", "Lkotlin/ULong;", "compareTo-VKZWuLQ", "(SJ)I", "compareTo-xj2QHRw", "(SS)I", "dec", "dec-Mh2AYeg", "div", "div-7apg3OU", "div-WZ4Q5Ns", "div-VKZWuLQ", "(SJ)J", "div-xj2QHRw", "equals", "", "", "equals-impl", "(SLjava/lang/Object;)Z", "floorDiv", "floorDiv-7apg3OU", "floorDiv-WZ4Q5Ns", "floorDiv-VKZWuLQ", "floorDiv-xj2QHRw", "hashCode", "hashCode-impl", "(S)I", "inc", "inc-Mh2AYeg", "inv", "inv-Mh2AYeg", "minus", "minus-7apg3OU", "minus-WZ4Q5Ns", "minus-VKZWuLQ", "minus-xj2QHRw", "mod", "mod-7apg3OU", "(SB)B", "mod-WZ4Q5Ns", "mod-VKZWuLQ", "mod-xj2QHRw", "or", "or-xj2QHRw", "plus", "plus-7apg3OU", "plus-WZ4Q5Ns", "plus-VKZWuLQ", "plus-xj2QHRw", "rangeTo", "Lkotlin/ranges/UIntRange;", "rangeTo-xj2QHRw", "(SS)Lkotlin/ranges/UIntRange;", "rangeUntil", "rangeUntil-xj2QHRw", "rem", "rem-7apg3OU", "rem-WZ4Q5Ns", "rem-VKZWuLQ", "rem-xj2QHRw", "times", "times-7apg3OU", "times-WZ4Q5Ns", "times-VKZWuLQ", "times-xj2QHRw", "toByte", "", "toByte-impl", "(S)B", "toDouble", "", "toDouble-impl", "(S)D", "toFloat", "", "toFloat-impl", "(S)F", "toInt", "toInt-impl", "toLong", "", "toLong-impl", "(S)J", "toShort", "toShort-impl", "toString", "", "toString-impl", "(S)Ljava/lang/String;", "toUByte", "toUByte-w2LRezQ", "toUInt", "toUInt-pVg5ArA", "toULong", "toULong-s-VKNKU", "toUShort", "toUShort-Mh2AYeg", "xor", "xor-xj2QHRw", "Companion", "kotlin-stdlib"}, k = 1, mv = {1, 8, 0}, xi = 48)
/* compiled from: UShort.kt */
public final class UShort implements Comparable<UShort> {
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    public static final short MAX_VALUE = -1;
    public static final short MIN_VALUE = 0;
    public static final int SIZE_BITS = 16;
    public static final int SIZE_BYTES = 2;
    private final short data;

    /* renamed from: box-impl  reason: not valid java name */
    public static final /* synthetic */ UShort m414boximpl(short s) {
        return new UShort(s);
    }

    /* renamed from: constructor-impl  reason: not valid java name */
    public static short m420constructorimpl(short s) {
        return s;
    }

    /* renamed from: equals-impl  reason: not valid java name */
    public static boolean m426equalsimpl(short s, Object obj) {
        return (obj instanceof UShort) && s == ((UShort) obj).m470unboximpl();
    }

    /* renamed from: equals-impl0  reason: not valid java name */
    public static final boolean m427equalsimpl0(short s, short s2) {
        return s == s2;
    }

    public static /* synthetic */ void getData$annotations() {
    }

    /* renamed from: hashCode-impl  reason: not valid java name */
    public static int m432hashCodeimpl(short s) {
        return Short.hashCode(s);
    }

    /* renamed from: toByte-impl  reason: not valid java name */
    private static final byte m458toByteimpl(short s) {
        return (byte) s;
    }

    /* renamed from: toDouble-impl  reason: not valid java name */
    private static final double m459toDoubleimpl(short s) {
        return (double) (s & MAX_VALUE);
    }

    /* renamed from: toFloat-impl  reason: not valid java name */
    private static final float m460toFloatimpl(short s) {
        return (float) (s & MAX_VALUE);
    }

    /* renamed from: toInt-impl  reason: not valid java name */
    private static final int m461toIntimpl(short s) {
        return s & MAX_VALUE;
    }

    /* renamed from: toLong-impl  reason: not valid java name */
    private static final long m462toLongimpl(short s) {
        return ((long) s) & 65535;
    }

    /* renamed from: toShort-impl  reason: not valid java name */
    private static final short m463toShortimpl(short s) {
        return s;
    }

    /* renamed from: toUShort-Mh2AYeg  reason: not valid java name */
    private static final short m468toUShortMh2AYeg(short s) {
        return s;
    }

    public boolean equals(Object obj) {
        return m426equalsimpl(this.data, obj);
    }

    public int hashCode() {
        return m432hashCodeimpl(this.data);
    }

    /* renamed from: unbox-impl  reason: not valid java name */
    public final /* synthetic */ short m470unboximpl() {
        return this.data;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object obj) {
        return Intrinsics.compare((int) m470unboximpl() & MAX_VALUE, (int) ((UShort) obj).m470unboximpl() & MAX_VALUE);
    }

    private /* synthetic */ UShort(short s) {
        this.data = s;
    }

    @Metadata(d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u0016\u0010\u0003\u001a\u00020\u0004XTø\u0001\u0000ø\u0001\u0001¢\u0006\u0004\n\u0002\u0010\u0005R\u0016\u0010\u0006\u001a\u00020\u0004XTø\u0001\u0000ø\u0001\u0001¢\u0006\u0004\n\u0002\u0010\u0005R\u000e\u0010\u0007\u001a\u00020\bXT¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bXT¢\u0006\u0002\n\u0000\u0002\b\n\u0002\b\u0019\n\u0002\b!¨\u0006\n"}, d2 = {"Lkotlin/UShort$Companion;", "", "()V", "MAX_VALUE", "Lkotlin/UShort;", "S", "MIN_VALUE", "SIZE_BITS", "", "SIZE_BYTES", "kotlin-stdlib"}, k = 1, mv = {1, 8, 0}, xi = 48)
    /* compiled from: UShort.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }

    /* renamed from: compareTo-7apg3OU  reason: not valid java name */
    private static final int m415compareTo7apg3OU(short s, byte b) {
        return Intrinsics.compare((int) s & MAX_VALUE, (int) b & UByte.MAX_VALUE);
    }

    /* renamed from: compareTo-xj2QHRw  reason: not valid java name */
    private int m418compareToxj2QHRw(short s) {
        return Intrinsics.compare((int) m470unboximpl() & MAX_VALUE, (int) s & MAX_VALUE);
    }

    /* renamed from: compareTo-xj2QHRw  reason: not valid java name */
    private static int m419compareToxj2QHRw(short s, short s2) {
        return Intrinsics.compare((int) s & MAX_VALUE, (int) s2 & MAX_VALUE);
    }

    /* renamed from: compareTo-WZ4Q5Ns  reason: not valid java name */
    private static final int m417compareToWZ4Q5Ns(short s, int i) {
        return Integer.compare(UInt.m234constructorimpl(s & MAX_VALUE) ^ Integer.MIN_VALUE, i ^ Integer.MIN_VALUE);
    }

    /* renamed from: compareTo-VKZWuLQ  reason: not valid java name */
    private static final int m416compareToVKZWuLQ(short s, long j) {
        return UByte$$ExternalSyntheticBackport0.m(ULong.m313constructorimpl(((long) s) & 65535), j);
    }

    /* renamed from: plus-7apg3OU  reason: not valid java name */
    private static final int m444plus7apg3OU(short s, byte b) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) + UInt.m234constructorimpl(b & UByte.MAX_VALUE));
    }

    /* renamed from: plus-xj2QHRw  reason: not valid java name */
    private static final int m447plusxj2QHRw(short s, short s2) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) + UInt.m234constructorimpl(s2 & MAX_VALUE));
    }

    /* renamed from: plus-WZ4Q5Ns  reason: not valid java name */
    private static final int m446plusWZ4Q5Ns(short s, int i) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) + i);
    }

    /* renamed from: plus-VKZWuLQ  reason: not valid java name */
    private static final long m445plusVKZWuLQ(short s, long j) {
        return ULong.m313constructorimpl(ULong.m313constructorimpl(((long) s) & 65535) + j);
    }

    /* renamed from: minus-7apg3OU  reason: not valid java name */
    private static final int m435minus7apg3OU(short s, byte b) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) - UInt.m234constructorimpl(b & UByte.MAX_VALUE));
    }

    /* renamed from: minus-xj2QHRw  reason: not valid java name */
    private static final int m438minusxj2QHRw(short s, short s2) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) - UInt.m234constructorimpl(s2 & MAX_VALUE));
    }

    /* renamed from: minus-WZ4Q5Ns  reason: not valid java name */
    private static final int m437minusWZ4Q5Ns(short s, int i) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) - i);
    }

    /* renamed from: minus-VKZWuLQ  reason: not valid java name */
    private static final long m436minusVKZWuLQ(short s, long j) {
        return ULong.m313constructorimpl(ULong.m313constructorimpl(((long) s) & 65535) - j);
    }

    /* renamed from: times-7apg3OU  reason: not valid java name */
    private static final int m454times7apg3OU(short s, byte b) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) * UInt.m234constructorimpl(b & UByte.MAX_VALUE));
    }

    /* renamed from: times-xj2QHRw  reason: not valid java name */
    private static final int m457timesxj2QHRw(short s, short s2) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) * UInt.m234constructorimpl(s2 & MAX_VALUE));
    }

    /* renamed from: times-WZ4Q5Ns  reason: not valid java name */
    private static final int m456timesWZ4Q5Ns(short s, int i) {
        return UInt.m234constructorimpl(UInt.m234constructorimpl(s & MAX_VALUE) * i);
    }

    /* renamed from: times-VKZWuLQ  reason: not valid java name */
    private static final long m455timesVKZWuLQ(short s, long j) {
        return ULong.m313constructorimpl(ULong.m313constructorimpl(((long) s) & 65535) * j);
    }

    /* renamed from: div-7apg3OU  reason: not valid java name */
    private static final int m422div7apg3OU(short s, byte b) {
        return UByte$$ExternalSyntheticBackport0.m$1(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(b & UByte.MAX_VALUE));
    }

    /* renamed from: div-xj2QHRw  reason: not valid java name */
    private static final int m425divxj2QHRw(short s, short s2) {
        return UByte$$ExternalSyntheticBackport0.m$1(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(s2 & MAX_VALUE));
    }

    /* renamed from: div-WZ4Q5Ns  reason: not valid java name */
    private static final int m424divWZ4Q5Ns(short s, int i) {
        return UByte$$ExternalSyntheticBackport0.m$1(UInt.m234constructorimpl(s & MAX_VALUE), i);
    }

    /* renamed from: div-VKZWuLQ  reason: not valid java name */
    private static final long m423divVKZWuLQ(short s, long j) {
        return UByte$$ExternalSyntheticBackport0.m$1(ULong.m313constructorimpl(((long) s) & 65535), j);
    }

    /* renamed from: rem-7apg3OU  reason: not valid java name */
    private static final int m450rem7apg3OU(short s, byte b) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(b & UByte.MAX_VALUE));
    }

    /* renamed from: rem-xj2QHRw  reason: not valid java name */
    private static final int m453remxj2QHRw(short s, short s2) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(s2 & MAX_VALUE));
    }

    /* renamed from: rem-WZ4Q5Ns  reason: not valid java name */
    private static final int m452remWZ4Q5Ns(short s, int i) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m234constructorimpl(s & MAX_VALUE), i);
    }

    /* renamed from: rem-VKZWuLQ  reason: not valid java name */
    private static final long m451remVKZWuLQ(short s, long j) {
        return UByte$$ExternalSyntheticBackport0.m(ULong.m313constructorimpl(((long) s) & 65535), j);
    }

    /* renamed from: floorDiv-7apg3OU  reason: not valid java name */
    private static final int m428floorDiv7apg3OU(short s, byte b) {
        return UByte$$ExternalSyntheticBackport0.m$1(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(b & UByte.MAX_VALUE));
    }

    /* renamed from: floorDiv-xj2QHRw  reason: not valid java name */
    private static final int m431floorDivxj2QHRw(short s, short s2) {
        return UByte$$ExternalSyntheticBackport0.m$1(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(s2 & MAX_VALUE));
    }

    /* renamed from: floorDiv-WZ4Q5Ns  reason: not valid java name */
    private static final int m430floorDivWZ4Q5Ns(short s, int i) {
        return UByte$$ExternalSyntheticBackport0.m$1(UInt.m234constructorimpl(s & MAX_VALUE), i);
    }

    /* renamed from: floorDiv-VKZWuLQ  reason: not valid java name */
    private static final long m429floorDivVKZWuLQ(short s, long j) {
        return UByte$$ExternalSyntheticBackport0.m$1(ULong.m313constructorimpl(((long) s) & 65535), j);
    }

    /* renamed from: mod-7apg3OU  reason: not valid java name */
    private static final byte m439mod7apg3OU(short s, byte b) {
        return UByte.m157constructorimpl((byte) UByte$$ExternalSyntheticBackport0.m(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(b & UByte.MAX_VALUE)));
    }

    /* renamed from: mod-xj2QHRw  reason: not valid java name */
    private static final short m442modxj2QHRw(short s, short s2) {
        return m420constructorimpl((short) UByte$$ExternalSyntheticBackport0.m(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(s2 & MAX_VALUE)));
    }

    /* renamed from: mod-WZ4Q5Ns  reason: not valid java name */
    private static final int m441modWZ4Q5Ns(short s, int i) {
        return UByte$$ExternalSyntheticBackport0.m(UInt.m234constructorimpl(s & MAX_VALUE), i);
    }

    /* renamed from: mod-VKZWuLQ  reason: not valid java name */
    private static final long m440modVKZWuLQ(short s, long j) {
        return UByte$$ExternalSyntheticBackport0.m(ULong.m313constructorimpl(((long) s) & 65535), j);
    }

    /* renamed from: inc-Mh2AYeg  reason: not valid java name */
    private static final short m433incMh2AYeg(short s) {
        return m420constructorimpl((short) (s + 1));
    }

    /* renamed from: dec-Mh2AYeg  reason: not valid java name */
    private static final short m421decMh2AYeg(short s) {
        return m420constructorimpl((short) (s - 1));
    }

    /* renamed from: rangeTo-xj2QHRw  reason: not valid java name */
    private static final UIntRange m448rangeToxj2QHRw(short s, short s2) {
        return new UIntRange(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(s2 & MAX_VALUE), (DefaultConstructorMarker) null);
    }

    /* renamed from: rangeUntil-xj2QHRw  reason: not valid java name */
    private static final UIntRange m449rangeUntilxj2QHRw(short s, short s2) {
        return URangesKt.m1415untilJ1ME1BU(UInt.m234constructorimpl(s & MAX_VALUE), UInt.m234constructorimpl(s2 & MAX_VALUE));
    }

    /* renamed from: and-xj2QHRw  reason: not valid java name */
    private static final short m413andxj2QHRw(short s, short s2) {
        return m420constructorimpl((short) (s & s2));
    }

    /* renamed from: or-xj2QHRw  reason: not valid java name */
    private static final short m443orxj2QHRw(short s, short s2) {
        return m420constructorimpl((short) (s | s2));
    }

    /* renamed from: xor-xj2QHRw  reason: not valid java name */
    private static final short m469xorxj2QHRw(short s, short s2) {
        return m420constructorimpl((short) (s ^ s2));
    }

    /* renamed from: inv-Mh2AYeg  reason: not valid java name */
    private static final short m434invMh2AYeg(short s) {
        return m420constructorimpl((short) (~s));
    }

    /* renamed from: toUByte-w2LRezQ  reason: not valid java name */
    private static final byte m465toUBytew2LRezQ(short s) {
        return UByte.m157constructorimpl((byte) s);
    }

    /* renamed from: toUInt-pVg5ArA  reason: not valid java name */
    private static final int m466toUIntpVg5ArA(short s) {
        return UInt.m234constructorimpl(s & MAX_VALUE);
    }

    /* renamed from: toULong-s-VKNKU  reason: not valid java name */
    private static final long m467toULongsVKNKU(short s) {
        return ULong.m313constructorimpl(((long) s) & 65535);
    }

    /* renamed from: toString-impl  reason: not valid java name */
    public static String m464toStringimpl(short s) {
        return String.valueOf(s & MAX_VALUE);
    }

    public String toString() {
        return m464toStringimpl(this.data);
    }
}
