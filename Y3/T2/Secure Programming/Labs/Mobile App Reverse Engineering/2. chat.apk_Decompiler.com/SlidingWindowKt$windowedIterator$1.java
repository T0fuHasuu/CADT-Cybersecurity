package kotlin.collections;

import java.util.Iterator;
import java.util.List;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.sequences.SequenceScope;

@Metadata(d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u00020\u00040\u0003H@"}, d2 = {"<anonymous>", "", "T", "Lkotlin/sequences/SequenceScope;", ""}, k = 3, mv = {1, 8, 0}, xi = 48)
@DebugMetadata(c = "kotlin.collections.SlidingWindowKt$windowedIterator$1", f = "SlidingWindow.kt", i = {0, 0, 0, 2, 2, 3, 3}, l = {34, 40, 49, 55, 58}, m = "invokeSuspend", n = {"$this$iterator", "buffer", "gap", "$this$iterator", "buffer", "$this$iterator", "buffer"}, s = {"L$0", "L$1", "I$0", "L$0", "L$1", "L$0", "L$1"})
/* compiled from: SlidingWindow.kt */
final class SlidingWindowKt$windowedIterator$1 extends RestrictedSuspendLambda implements Function2<SequenceScope<? super List<? extends T>>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Iterator<T> $iterator;
    final /* synthetic */ boolean $partialWindows;
    final /* synthetic */ boolean $reuseBuffer;
    final /* synthetic */ int $size;
    final /* synthetic */ int $step;
    int I$0;
    private /* synthetic */ Object L$0;
    Object L$1;
    Object L$2;
    int label;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    SlidingWindowKt$windowedIterator$1(int i, int i2, Iterator<? extends T> it, boolean z, boolean z2, Continuation<? super SlidingWindowKt$windowedIterator$1> continuation) {
        super(2, continuation);
        this.$size = i;
        this.$step = i2;
        this.$iterator = it;
        this.$reuseBuffer = z;
        this.$partialWindows = z2;
    }

    public final Continuation<Unit> create(Object obj, Continuation<?> continuation) {
        SlidingWindowKt$windowedIterator$1 slidingWindowKt$windowedIterator$1 = new SlidingWindowKt$windowedIterator$1(this.$size, this.$step, this.$iterator, this.$reuseBuffer, this.$partialWindows, continuation);
        slidingWindowKt$windowedIterator$1.L$0 = obj;
        return slidingWindowKt$windowedIterator$1;
    }

    public final Object invoke(SequenceScope<? super List<? extends T>> sequenceScope, Continuation<? super Unit> continuation) {
        return ((SlidingWindowKt$windowedIterator$1) create(sequenceScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00f5  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0147  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0139 A[SYNTHETIC] */
    public final java.lang.Object invokeSuspend(java.lang.Object r12) {
        /*
            r11 = this;
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r11.label
            r2 = 5
            r3 = 4
            r4 = 3
            r5 = 2
            r6 = 1
            r7 = 0
            if (r1 == 0) goto L_0x005a
            if (r1 == r6) goto L_0x0046
            if (r1 == r5) goto L_0x0041
            if (r1 == r4) goto L_0x002f
            if (r1 == r3) goto L_0x0021
            if (r1 != r2) goto L_0x0019
            goto L_0x0041
        L_0x0019:
            java.lang.IllegalStateException r12 = new java.lang.IllegalStateException
            java.lang.String r0 = "call to 'resume' before 'invoke' with coroutine"
            r12.<init>(r0)
            throw r12
        L_0x0021:
            java.lang.Object r1 = r11.L$1
            kotlin.collections.RingBuffer r1 = (kotlin.collections.RingBuffer) r1
            java.lang.Object r4 = r11.L$0
            kotlin.sequences.SequenceScope r4 = (kotlin.sequences.SequenceScope) r4
            kotlin.ResultKt.throwOnFailure(r12)
            r12 = r11
            goto L_0x016b
        L_0x002f:
            java.lang.Object r1 = r11.L$2
            java.util.Iterator r1 = (java.util.Iterator) r1
            java.lang.Object r5 = r11.L$1
            kotlin.collections.RingBuffer r5 = (kotlin.collections.RingBuffer) r5
            java.lang.Object r8 = r11.L$0
            kotlin.sequences.SequenceScope r8 = (kotlin.sequences.SequenceScope) r8
            kotlin.ResultKt.throwOnFailure(r12)
            r12 = r11
            goto L_0x0133
        L_0x0041:
            kotlin.ResultKt.throwOnFailure(r12)
            goto L_0x018d
        L_0x0046:
            int r1 = r11.I$0
            java.lang.Object r2 = r11.L$2
            java.util.Iterator r2 = (java.util.Iterator) r2
            java.lang.Object r3 = r11.L$1
            java.util.ArrayList r3 = (java.util.ArrayList) r3
            java.lang.Object r4 = r11.L$0
            kotlin.sequences.SequenceScope r4 = (kotlin.sequences.SequenceScope) r4
            kotlin.ResultKt.throwOnFailure(r12)
            r8 = r11
        L_0x0058:
            r12 = r1
            goto L_0x00ac
        L_0x005a:
            kotlin.ResultKt.throwOnFailure(r12)
            java.lang.Object r12 = r11.L$0
            kotlin.sequences.SequenceScope r12 = (kotlin.sequences.SequenceScope) r12
            int r1 = r11.$size
            r8 = 1024(0x400, float:1.435E-42)
            int r1 = kotlin.ranges.RangesKt.coerceAtMost((int) r1, (int) r8)
            int r8 = r11.$step
            int r9 = r11.$size
            int r8 = r8 - r9
            if (r8 < 0) goto L_0x00e6
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>(r1)
            java.util.Iterator<T> r1 = r11.$iterator
            r3 = 0
            r4 = r12
            r12 = r3
            r3 = r2
            r2 = r1
            r1 = r8
            r8 = r11
        L_0x007e:
            boolean r9 = r2.hasNext()
            if (r9 == 0) goto L_0x00be
            java.lang.Object r9 = r2.next()
            if (r12 <= 0) goto L_0x008d
            int r12 = r12 + -1
            goto L_0x007e
        L_0x008d:
            r3.add(r9)
            int r9 = r3.size()
            int r10 = r8.$size
            if (r9 != r10) goto L_0x007e
            r12 = r8
            kotlin.coroutines.Continuation r12 = (kotlin.coroutines.Continuation) r12
            r8.L$0 = r4
            r8.L$1 = r3
            r8.L$2 = r2
            r8.I$0 = r1
            r8.label = r6
            java.lang.Object r12 = r4.yield(r3, r12)
            if (r12 != r0) goto L_0x0058
            return r0
        L_0x00ac:
            boolean r1 = r8.$reuseBuffer
            if (r1 == 0) goto L_0x00b4
            r3.clear()
            goto L_0x00bc
        L_0x00b4:
            java.util.ArrayList r1 = new java.util.ArrayList
            int r3 = r8.$size
            r1.<init>(r3)
            r3 = r1
        L_0x00bc:
            r1 = r12
            goto L_0x007e
        L_0x00be:
            r12 = r3
            java.util.Collection r12 = (java.util.Collection) r12
            boolean r12 = r12.isEmpty()
            r12 = r12 ^ r6
            if (r12 == 0) goto L_0x018d
            boolean r12 = r8.$partialWindows
            if (r12 != 0) goto L_0x00d4
            int r12 = r3.size()
            int r1 = r8.$size
            if (r12 != r1) goto L_0x018d
        L_0x00d4:
            r12 = r8
            kotlin.coroutines.Continuation r12 = (kotlin.coroutines.Continuation) r12
            r8.L$0 = r7
            r8.L$1 = r7
            r8.L$2 = r7
            r8.label = r5
            java.lang.Object r12 = r4.yield(r3, r12)
            if (r12 != r0) goto L_0x018d
            return r0
        L_0x00e6:
            kotlin.collections.RingBuffer r5 = new kotlin.collections.RingBuffer
            r5.<init>(r1)
            java.util.Iterator<T> r1 = r11.$iterator
            r8 = r12
            r12 = r11
        L_0x00ef:
            boolean r9 = r1.hasNext()
            if (r9 == 0) goto L_0x0139
            java.lang.Object r9 = r1.next()
            r5.add(r9)
            boolean r9 = r5.isFull()
            if (r9 == 0) goto L_0x00ef
            int r9 = r5.size()
            int r10 = r12.$size
            if (r9 >= r10) goto L_0x010f
            kotlin.collections.RingBuffer r5 = r5.expanded(r10)
            goto L_0x00ef
        L_0x010f:
            boolean r9 = r12.$reuseBuffer
            if (r9 == 0) goto L_0x0117
            r9 = r5
            java.util.List r9 = (java.util.List) r9
            goto L_0x0121
        L_0x0117:
            java.util.ArrayList r9 = new java.util.ArrayList
            r10 = r5
            java.util.Collection r10 = (java.util.Collection) r10
            r9.<init>(r10)
            java.util.List r9 = (java.util.List) r9
        L_0x0121:
            r10 = r12
            kotlin.coroutines.Continuation r10 = (kotlin.coroutines.Continuation) r10
            r12.L$0 = r8
            r12.L$1 = r5
            r12.L$2 = r1
            r12.label = r4
            java.lang.Object r9 = r8.yield(r9, r10)
            if (r9 != r0) goto L_0x0133
            return r0
        L_0x0133:
            int r9 = r12.$step
            r5.removeFirst(r9)
            goto L_0x00ef
        L_0x0139:
            boolean r1 = r12.$partialWindows
            if (r1 == 0) goto L_0x018d
            r1 = r5
            r4 = r8
        L_0x013f:
            int r5 = r1.size()
            int r8 = r12.$step
            if (r5 <= r8) goto L_0x0171
            boolean r5 = r12.$reuseBuffer
            if (r5 == 0) goto L_0x014f
            r5 = r1
            java.util.List r5 = (java.util.List) r5
            goto L_0x0159
        L_0x014f:
            java.util.ArrayList r5 = new java.util.ArrayList
            r8 = r1
            java.util.Collection r8 = (java.util.Collection) r8
            r5.<init>(r8)
            java.util.List r5 = (java.util.List) r5
        L_0x0159:
            r8 = r12
            kotlin.coroutines.Continuation r8 = (kotlin.coroutines.Continuation) r8
            r12.L$0 = r4
            r12.L$1 = r1
            r12.L$2 = r7
            r12.label = r3
            java.lang.Object r5 = r4.yield(r5, r8)
            if (r5 != r0) goto L_0x016b
            return r0
        L_0x016b:
            int r5 = r12.$step
            r1.removeFirst(r5)
            goto L_0x013f
        L_0x0171:
            r3 = r1
            java.util.Collection r3 = (java.util.Collection) r3
            boolean r3 = r3.isEmpty()
            r3 = r3 ^ r6
            if (r3 == 0) goto L_0x018d
            r3 = r12
            kotlin.coroutines.Continuation r3 = (kotlin.coroutines.Continuation) r3
            r12.L$0 = r7
            r12.L$1 = r7
            r12.L$2 = r7
            r12.label = r2
            java.lang.Object r12 = r4.yield(r1, r3)
            if (r12 != r0) goto L_0x018d
            return r0
        L_0x018d:
            kotlin.Unit r12 = kotlin.Unit.INSTANCE
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.collections.SlidingWindowKt$windowedIterator$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
