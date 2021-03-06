package mips_cpu_2nd

import chisel3._
import chisel3.stage._
import chisel3.util._
import os.makeDir
/*
目前已经禁用了swl和lwl相关的功能功能
若想启用，修改  Mem_withRL_Data := _dmem.io.RD
修改mem_write



*/
class myCPU extends RawModule with mips_macros {//
        //完全没用到chisel真正好的地方，我是废物呜呜呜呜    
    val   ext_int = IO(Input(UInt(6.W)))
    val   resetn  = IO(Input(Bool())).suggestName("resetn")
    val   clk    = IO(Input(Bool())).suggestName("clk")
    
    val   inst_cache = IO(Output(UInt(1.W)))
    val   inst_req = IO(Output(UInt(1.W))).suggestName("inst_sram_en")
    val   inst_wr = IO(Output(UInt(1.W))).suggestName("inst_sram_wen")
    val   inst_size = IO(Output(UInt(2.W)))
    val   inst_addr = IO(Output(UInt(32.W))).suggestName("inst_sram_addr")
    val   inst_wdata = IO(Output(UInt(32.W))).suggestName("inst_sram_wdata")
    val   inst_addr_ok = IO(Input(UInt(1.W)))
    val   inst_data_ok = IO(Input(UInt(1.W)))
    val   inst_rdata_L = IO(Input(UInt(32.W))).suggestName("inst_sram_rdata_L") 
    val   inst_rdata_M = IO(Input(UInt(32.W))).suggestName("inst_sram_rdata_M") 
    val   inst_rdata_R = IO(Input(UInt(32.W))).suggestName("inst_sram_rdata_R") 
    val   inst_hit = IO(Input(UInt(1.W))).suggestName("inst_sram_hit")
    val   inst_valid = IO(Input(UInt(3.W))).suggestName("inst_data_valid")//代表回来的数据哪一位有效
    val   inst_write_en = IO(Input(UInt(3.W))).suggestName("inst_data_valid")//代表回来的数据哪一位有效
    //"b001" only L valid "b011" L and M valid 
    //"b111" L M R all valid 
   
//     val   data_req = IO(Output(UInt(1.W))).suggestName("data_sram_en")
//     val   data_wr = IO(Output(UInt(1.W))).suggestName("data_sram_wen")
//     val   data_size = IO(Output(UInt(2.W)))
//     val   data_addr = IO(Output(UInt(32.W))).suggestName("data_sram_addr")
//     val   data_wdata = IO(Output(UInt(32.W))).suggestName("data_sram_wdata")
//     val   data_cache = IO(Output(UInt(1.W)))
//     val   data_addr_ok = IO(Input(UInt(1.W)))
//     val   data_data_ok = IO(Input(UInt(1.W)))
//     val   data_rdata = IO(Input(UInt(32.W))).suggestName("data_sram_rdata") 
//    // val   data_hit = IO(Input(UInt(1.W)))
   
    val   debug_wb_pc       = IO(Vec(2,Output(UInt(32.W))))
    val   debug_wb_rf_wen   = IO(Vec(2,Output(UInt(4.W))))
    val   debug_wb_rf_wnum  = IO(Vec(2,Output(UInt(5.W))))
    val   debug_wb_rf_wdata = IO(Vec(2,Output(UInt(32.W))))
        

    override def desiredName = "myCPU"
    withClockAndReset(clk.asClock,(~resetn).asAsyncReset) {
//定义一些可以复用的类
class pc_in_bundle extends Bundle {
    val pc_value_in = Input(UInt(32.W))
    val pc_inst_in = Input(UInt(32.W))
}
class pc_out_bundle extends Bundle {
    val pc_value_out = Output(UInt(32.W))
    val pc_inst_out = Output(UInt(32.W))
}
class pc_detail extends Module {
    val io = IO(new Bundle{
        val stall = Input(Bool())
        val flush = Input(Bool())   
    })
    val io_in = new pc_in_bundle
    val io_out = new pc_out_bundle
    val pc_value = RegInit(UInt(32.W))
    pc_value := Mux(io.flush,0.U,Mux(io.stall,io_in.pc_value_in,pc_value))
    io_out.pc_value_out := pc_value
    val pc_inst = RegInit(UInt(32.W))
    pc_inst  := Mux(io.flush,0.U,Mux(io.stall,io_in.pc_inst_in,pc_inst))
    io_out.pc_inst_out  := pc_inst
}

// ------实例化所有模块,并且连接部分线
val inst_cache = Module(new inst_cache_2nd).io



// -----------定义所有相关的变量-------
//-------------pc_cal ----------
val pc_fetch = Wire(UInt(32.W))
val pc_banch_prediction = Wire(UInt(32.W))

//-------------fec_1 在这个阶段进行分支预测的计算----------
val stage_fec_1_stall = Wire(Bool())
val stage_fec_1_flush = Wire(Bool())
val pc_valid = Mux(pc_fetch(4,2) <= 5.U,"b111".U,Mux(pc_fetch(4,2) === 6.U,"b011".U,"b001".U))
val bru = Module(new branch_prediction).io
val stage_fec_1_pc_L = Module(new pc_detail)
val stage_fec_1_pc_M = Module(new pc_detail)
val stage_fec_1_pc_R = Module(new pc_detail)
val stage_fec_1_pc_valid = RegInit(UInt(3.W))

stage_fec_1_pc_valid := Mux(stage_fec_1_flush,0.U,Mux(stage_fec_1_stall,pc_valid,stage_fec_1_pc_valid))

stage_fec_1_pc_L.io_in.pc_inst_in := 0.U
stage_fec_1_pc_L.io_in.pc_value_in := pc_fetch
stage_fec_1_pc_M.io_in.pc_inst_in := 0.U
stage_fec_1_pc_M.io_in.pc_value_in := Cat(pc_fetch(31,5),pc_fetch(4,2) + 1.U ,pc_fetch(1,0))
stage_fec_1_pc_R.io_in.pc_inst_in := 0.U
stage_fec_1_pc_R.io_in.pc_value_in := Cat(pc_fetch(31,5),pc_fetch(4,2) + 2.U ,pc_fetch(1,0))
//flush and stall
stage_fec_1_pc_L.io.flush := stage_fec_1_flush
stage_fec_1_pc_M.io.flush := stage_fec_1_flush
stage_fec_1_pc_R.io.flush := stage_fec_1_flush

stage_fec_1_pc_L.io.stall := stage_fec_1_stall
stage_fec_1_pc_M.io.stall := stage_fec_1_stall
stage_fec_1_pc_R.io.stall := stage_fec_1_stall

bru.pc := stage_fec_1_pc_L.io_out.pc_value_out
bru.pc_plus := stage_fec_1_pc_M.io_out.pc_value_out
bru.pc_plus_plus := stage_fec_1_pc_R.io_out.pc_value_out
val branch_state = Wire(Vec(3,Bool()))

branch_state(0) := Mux(stage_fec_1_pc_valid(0),bru.out_L,0.U)
branch_state(1) := Mux(stage_fec_1_pc_valid(1),bru.out_M,0.U)
branch_state(2) := Mux(stage_fec_1_pc_valid(2),bru.out_R,0.U)


//-------------fec_2----------
//this stage,we push 3 insts to the inst buffer, and make 2 insts out

val stage_fec_2_stall = Wire(Bool())
val stage_fec_2_flush = Wire(Bool())
val inst_buffer = Module(new fifo(32,64,3,2)).io
val stage_fec_2_pc_L = Module(new pc_detail)
val stage_fec_2_pc_M = Module(new pc_detail)
val stage_fec_2_pc_R = Module(new pc_detail)

stage_fec_2_pc_L.io_in.pc_inst_in := stage_fec_1_pc_L.io_out.pc_inst_out
stage_fec_2_pc_L.io_in.pc_value_in := stage_fec_1_pc_L.io_out.pc_value_out

stage_fec_2_pc_M.io_in.pc_inst_in := stage_fec_1_pc_M.io_out.pc_inst_out
stage_fec_2_pc_M.io_in.pc_value_in := stage_fec_1_pc_M.io_out.pc_value_out 

stage_fec_2_pc_R.io_in.pc_inst_in := stage_fec_1_pc_R.io_out.pc_inst_out
stage_fec_2_pc_R.io_in.pc_value_in := stage_fec_1_pc_R.io_out.pc_value_out 

//flush and stall
stage_fec_2_pc_L.io.flush := stage_fec_2_flush
stage_fec_2_pc_M.io.flush := stage_fec_2_flush
stage_fec_2_pc_R.io.flush := stage_fec_2_flush

stage_fec_2_pc_L.io.stall := stage_fec_2_stall
stage_fec_2_pc_M.io.stall := stage_fec_2_stall
stage_fec_2_pc_R.io.stall := stage_fec_2_stall


    //read_en 的计算还是放到cache那边来进行计算把，还是得关注计算这些东西的延时
inst_buffer.write_en := inst_write_en
inst_buffer.write_in(0) := Cat(inst_rdata_L,stage_fec_2_pc_L.io_out.pc_value_out)
inst_buffer.write_in(1) := Cat(inst_rdata_M,stage_fec_2_pc_M.io_out.pc_value_out)
inst_buffer.write_in(2) := Cat(inst_rdata_R,stage_fec_2_pc_R.io_out.pc_value_out)
//full是用来控制前面流水线停止的，和这里无关

//------------------decoder---------------

class connector_with_inst extends Module{ 
    val io = IO(new Bundle{
        val stall = Input(Bool())
        val flush = Input(Bool())   
    })

    val io_in =   IO(Flipped(new decoder_port) )
    val io_out =   IO(new decoder_port) 

    val src1_has =  RegInit(0.U.asBool)
    val src2_has =  RegInit(0.U.asBool)
    val dest_has =  RegInit(0.U.asBool)
    //===================   ==========  =========================    =====

    val ALUCtrlD    = RegInit(0.U(32.W)) // 独热码
    val src1        = RegInit(0.U(5.W))
    val src2        = RegInit(0.U(5.W))
    val dest        = RegInit(0.U(5.W))
    val inst_opcode = Vec(7,RegInit(0.U(6.W)))
    val inst_type   = Vec(7,RegInit(0.U.asBool))
    val imm_value   = RegInit(0.U(32.W))
    val BadInstrD   = RegInit(0.U.asBool)

// =================================================================
    ALUCtrlD := Mux(io.flush,0.U,Mux(io.stall,io_in.ALUCtrlD,ALUCtrlD))
    src1 := Mux(io.flush,0.U,Mux(io.stall,io_in.src1,src1))
    src2 := Mux(io.flush,0.U,Mux(io.stall,io_in.src2,src2))
    dest := Mux(io.flush,0.U,Mux(io.stall,io_in.dest,dest))
    inst_opcode := Mux(io.flush,0.U,Mux(io.stall,io_in.inst_opcode,inst_opcode))
    inst_type := Mux(io.flush,0.U,Mux(io.stall,io_in.inst_type,inst_type))
    src1_has := Mux(io.flush,0.U,Mux(io.stall,io_in.ALUCtrlD,ALUCtrlD))
    src2_has := Mux(io.flush,0.U,Mux(io.stall,io_in.src1,src1))
    dest_has := Mux(io.flush,0.U,Mux(io.stall,io_in.dest_has,dest_has))
    imm_value := Mux(io.flush,0.U,Mux(io.stall,io_in.imm_value,imm_value))
    BadInstrD := Mux(io.flush,0.U,Mux(io.stall,io_in.BadInstrD,BadInstrD))
    
    io_out.ALUCtrlD := ALUCtrlD
    io_out.src1 := src1
    io_out.src2 := src2
    io_out.dest := dest
    io_out.inst_opcode := inst_opcode
    io_out.inst_type := inst_type
    io_out.imm_value := imm_value
    io_out.src1_has := src1_has
    io_out.src1_has := src1_has
    io_out.dest_has := dest_has
    io_out.BadInstrD := BadInstrD
}
val stage_decoder_stall = Wire(Bool())
val stage_decoder_flush = Wire(Bool())
val stage_decoder_pc = Seq.fill(2)(Module(new pc_detail))
for(i <- 0 to 1) {
    stage_decoder_pc(i).io_in.pc_inst_in := inst_buffer.read_out(i)(61,32)
    stage_decoder_pc(i).io_in.pc_value_in := inst_buffer.read_out(i)(31,0)
    stage_decoder_pc(i).io.flush := stage_decoder_flush
    stage_decoder_pc(i).io.stall := stage_decoder_stall   
}
val cu_decoder = Seq.fill(2)(Module(new cu)) // 两个解码模块
val decoder2reg_renaming = Seq.fill(2)(Module(new connector_with_inst)) // 两个流水线控制模块
for(i<- 0 to 1) {
    cu_decoder(i).io1.InstrD := stage_decoder_pc(i).io_out.pc_inst_out
    decoder2reg_renaming(i).io_in <> cu_decoder(i).io //decoder 2 reg_renaming
    decoder2reg_renaming(i).io.stall := stage_decoder_stall
    decoder2reg_renaming(i).io.flush := stage_decoder_flush
}

// flush and stall

    
//-------------reg renaming----------
val reg_renaming_stall = Wire(Bool())
val reg_renaming_flush = Wire(Bool())
val reg_rename = Module(new reg_renaming).io
val reg_renaming2issue =  Seq.fill(2)(Module(new connector_with_inst)) 
reg_rename.dest_has := Cat(decoder2reg_renaming(1).io_out.dest_has,decoder2reg_renaming(0).io_out.dest_has)
for(i <- 0 to 1) {
    reg_rename.src_in(i*2) := decoder2reg_renaming(i).io_out.src1
    reg_rename.src_in(i*2 + 1) := decoder2reg_renaming(i).io_out.src2  
    reg_rename.dest_in(i) := decoder2reg_renaming(i).io_out.dest
    reg_renaming2issue(i).io_in.src1 := reg_rename.src_out(i*2)
    reg_renaming2issue(i).io_in.src2 := reg_rename.src_out(i*2 + 1)
    reg_renaming2issue(i).io_in.dest := reg_rename.dest_out(i)

    reg_renaming2issue(i).io_in.src1_has := decoder2reg_renaming(i).io_out.src1_has
    reg_renaming2issue(i).io_in.src2_has := decoder2reg_renaming(i).io_out.src2_has
    reg_renaming2issue(i).io_in.dest_has := decoder2reg_renaming(i).io_out.dest_has

    reg_renaming2issue(i).io_in.imm_value := decoder2reg_renaming(i).io_out.imm_value
    reg_renaming2issue(i).io_in.inst_type := decoder2reg_renaming(i).io_out.inst_type
    reg_renaming2issue(i).io_in.BadInstrD := decoder2reg_renaming(i).io_out.BadInstrD
    reg_renaming2issue(i).io_in.inst_opcode := decoder2reg_renaming(i).io_out.inst_opcode
    reg_renaming2issue(i).io_in.ALUCtrlD := decoder2reg_renaming(i).io_out.ALUCtrlD
    
}

//-------------issue----------
//主要issue queue肯定是在alu queue里面的

val muldiv_issue_queue = Module (new issue_queue(32))
val branch_issue_queue = Module (new issue_queue(32))
val mem_issue_queue    = Module (new issue_queue(32))
val alu_issue_queue    = Module (new issue_queue(32))
val self_in_issue_queue      = Module (new issue_queue(32))
val privilege_issue_queue    = Module (new issue_queue(32))
val nop_issue_queue          = Module (new issue_queue(32))

muldiv_issue_queue.io.inst_write := Cat(0.U(1.W),reg_renaming2issue(0).io_out.inst_opcode(opcode_muldiv)) +
     reg_renaming2issue(1).io_out.inst_opcode(opcode_muldiv)
branch_issue_queue.io.inst_write := Cat(0.U(1.W),reg_renaming2issue(0).io_out.inst_opcode(opcode_branch)) +
     reg_renaming2issue(1).io_out.inst_opcode(opcode_branch)
mem_issue_queue.io.inst_write := Cat(0.U(1.W),reg_renaming2issue(0).io_out.inst_opcode(opcode_mem)) +
     reg_renaming2issue(1).io_out.inst_opcode(opcode_mem)
alu_issue_queue.io.inst_write := Cat(0.U(1.W),reg_renaming2issue(0).io_out.inst_opcode(opcode_alu)) +
     reg_renaming2issue(1).io_out.inst_opcode(opcode_alu)
self_in_issue_queue.io.inst_write := Cat(0.U(1.W),reg_renaming2issue(0).io_out.inst_opcode(opcode_self_in)) +
     reg_renaming2issue(1).io_out.inst_opcode(opcode_self_in)
privilege_issue_queue.io.inst_write := Cat(0.U(1.W),reg_renaming2issue(0).io_out.inst_opcode(opcode_privilege)) +
     reg_renaming2issue(1).io_out.inst_opcode(opcode_privilege)
nop_issue_queue.io.inst_write := Cat(0.U(1.W),reg_renaming2issue(0).io_out.inst_opcode(opcode_nop)) +
     reg_renaming2issue(1).io_out.inst_opcode(opcode_nop)
    



for( i <- 0 to 1) {


}
   
// ---------- PC ----------
  


//-------------IF----------
  
    
//-------------ID----------
    


//-------------EX----------


// ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！

//-------------MEM----------
  
  





    // -----------------others that I can not understand
}
}

// object myCPU_test extends App{//     (new ChiselStage).emitVerilog(new myCPU)
// }
