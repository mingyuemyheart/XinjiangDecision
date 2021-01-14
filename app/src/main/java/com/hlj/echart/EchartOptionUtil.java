package com.hlj.echart;

import android.text.TextUtils;
import android.util.Log;

import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.PointerType;
import com.github.abel533.echarts.code.Position;
import com.github.abel533.echarts.code.SelectedMode;
import com.github.abel533.echarts.code.SeriesType;
import com.github.abel533.echarts.code.Tool;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.json.GsonOption;
import com.github.abel533.echarts.series.Bar;
import com.github.abel533.echarts.series.Pie;
import com.github.abel533.echarts.style.ItemStyle;
import com.github.abel533.echarts.style.TextStyle;
import com.hlj.dto.WarningDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EchartOptionUtil {

    /**
     * 饼图
     * @param dataList
     * @return
     */
    public static GsonOption pieOption(ArrayList<WarningDto> dataList) {
        int pro = 0,city = 0,dis = 0;
        for (int i = 0; i < dataList.size(); i++) {
            WarningDto dto = dataList.get(i);
            if (TextUtils.equals(dto.item0.substring(2,6), "0000")) {
                pro++;
            } else if (!TextUtils.equals(dto.item0.substring(2,4), "00") && TextUtils.equals(dto.item0.substring(4,6), "00")) {
                city++;
            } else {
                dis++;
            }
        }

        String title = "";
        List<WarningDto> list = new ArrayList<>();
        WarningDto dto;
        if (pro > 0) {
            dto = new WarningDto();
            dto.name = "省";
            dto.color = "#4063B5";
            dto.count = pro;
            list.add(dto);
        }
        if (city > 0) {
            dto = new WarningDto();
            dto.name = "市";
            dto.color = "#E7BE64";
            dto.count = city;
            list.add(dto);
        }
        if (dis > 0) {
            dto = new WarningDto();
            dto.name = "县";
            dto.color = "#DC8E4F";
            dto.count = dis;
            list.add(dto);
        }
        String[] levelNames = new String[list.size()];
        String[] levelColors = new String[list.size()];
        int[] levelCounts = new int[list.size()];
//        String[] levelNames = {"省","市","县"};
//        String[] levelColors = {"#4063B5","#E7BE64","#DC8E4F"};
//        int[] levelCounts = {pro,city,dis};

        for (int i = 0; i < list.size(); i++) {
            WarningDto data = list.get(i);
            levelNames[i] = data.name;
            levelColors[i] = data.color;
            levelCounts[i] = data.count;
        }

        GsonOption option = new GsonOption();//创建option对象
        option.color(levelColors);
        option.title().text(title).subtext("").x("center");//设置标题  二级标题  标题位置
        option.toolbox().show(true).feature(Tool.mark);//设置工具栏 展示  能标记
        option.tooltip().show(true).formatter("{c}  {d}%");//设置显示工具格式
//        option.legend().data(levelNames).y("bottom").orient(Orient.horizontal);//设置图例  图例位置  图例对齐方式 竖列对齐

        Pie innerPie = new Pie();//创建饼图对象
//        ItemStyle itemStyle = new ItemStyle();
//        itemStyle.normal().position(Position.inside).show(false);
        innerPie.name(title).radius("0","60%").center("50%","50%");//设置饼图的标题 半径、位置
        //填充数据
        for(int i = 0; i < levelNames.length; i++){
            Map<String,Object> map = new HashMap<>();
            map.put("value",levelCounts[i]);//填充饼图数据
            map.put("name",levelNames[i]);//填充饼图数据对应的搜索引擎
            innerPie.data(map);
        }
        option.series(innerPie); //设置数据

        return option;
    }

    /**
     * 嵌套饼图
     * @param dataList
     * @return
     */
    public static GsonOption nestedPieOption(ArrayList<WarningDto> dataList) {
        //获取所有的预警类型
        HashMap<String, WarningDto> warningTypes = new HashMap<>();
        List<WarningDto> legendList = new ArrayList<>();
        List<WarningDto> colorList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            WarningDto dto = dataList.get(i);
            WarningDto typeDto = new WarningDto();
            typeDto.type = dto.type;
            typeDto.color = dto.typeColor;
            String name = dto.name;
            if (!TextUtils.isEmpty(dto.name)) {
                if (dto.name.contains("发布") && dto.name.contains("预警")) {
                    name = dto.name.substring(dto.name.indexOf("发布")+2, dto.name.indexOf("预警")-2) + "预警";
                }
            }

            typeDto.name = name;
            warningTypes.put(dto.type, typeDto);
        }

        //填充图例表格数据
        legendList.clear();
        for (Map.Entry<String, WarningDto> entry : warningTypes.entrySet()) {
            WarningDto legend = entry.getValue();
            legendList.add(legend);
        }

        colorList.clear();
        WarningDto color = new WarningDto();
        color.type = "01";
        color.name = "蓝色预警";
        color.color = "#1D67C1";
        colorList.add(color);
        color = new WarningDto();
        color.type = "02";
        color.name = "黄色预警";
        color.color = "#F7BA34";
        colorList.add(color);
        color = new WarningDto();
        color.type = "03";
        color.name = "橙色预警";
        color.color = "#F98227";
        colorList.add(color);
        color = new WarningDto();
        color.type = "04";
        color.name = "红色预警";
        color.color = "#D4292A";
        colorList.add(color);
        color = new WarningDto();
        color.type = "05";
        color.name = "未知颜色";
        colorList.add(color);


        for (int i = 0; i < dataList.size(); i++) {
            WarningDto dto = dataList.get(i);

            for (int j = 0; j < legendList.size(); j++) {
                WarningDto legend = legendList.get(j);
                if (TextUtils.equals(legend.type, dto.type)) {
                    legend.count++;
                }
            }

            for (int j = 0; j < colorList.size(); j++) {
                WarningDto c = colorList.get(j);
                if (TextUtils.equals(c.type, dto.color)) {
                    c.count++;
                }
            }
        }

        //内圈数据
        String title = "";
        String[] levelNames = new String[colorList.size()];
        String[] levelColors = new String[colorList.size()];
        int[] levelCounts = new int[colorList.size()];
//        String[] levelNames = {"蓝色预警","黄色预警","橙色预警","红色预警","其它"};
//        String[] levelColors = {"#1D67C1","#F7BA34","#F98227","#D4292A","#B4B6B7"};
//        int[] levelCounts = {blue,yellow,orange,red,cother};
        for (int i = 0; i < colorList.size(); i++) {
            WarningDto data = colorList.get(i);
            levelNames[i] = data.name;
            levelColors[i] = data.color;
            levelCounts[i] = data.count;
        }

        GsonOption option = new GsonOption();//创建option对象
        option.color(levelColors);
        option.title().text(title).subtext("").x("center");//设置标题  二级标题  标题位置
        option.toolbox().show(true).feature(Tool.mark);//设置工具栏 展示  能标记
        option.tooltip().show(false).formatter("{c}  {d}%");//设置显示工具格式
//        option.legend().data(levelNames).y("bottom").orient(Orient.horizontal);//设置图例  图例位置  图例对齐方式 竖列对齐

        Pie innerPie = new Pie();//创建饼图对象
        ItemStyle itemStyle = new ItemStyle();
        itemStyle.normal().position(Position.inside).show(false);
        innerPie.name(title).radius("0","40%").center("50%","50%").label(itemStyle);//设置饼图的标题 半径、位置
        //填充数据
        for(int i = 0; i < colorList.size(); i++){
            WarningDto c = colorList.get(i);
            Map<String,Object> map = new HashMap<>();
            map.put("value",c.count);//填充饼图数据
            map.put("name",c.name);//填充饼图数据对应的搜索引擎
            innerPie.data(map);
        }
        option.series(innerPie); //设置数据


        //外圈数据
        title = "";
        String[] typeNames = new String[legendList.size()];
        String[] typeColors = new String[legendList.size()];
        int[] typeCounts = new int[legendList.size()];
//        String[] typeNames = {"暴雨预警","暴雪预警","寒潮预警","大风预警","冰雹预警","大雾预警","霾预警","雷雨大风","道路结冰","森林火险","雷电预警","其它"};
//        String[] typeColors = {"#4383AE","#8E59B2","#554EAD","#C77B2D","#7CC0A7","#6B6C6D","#814E4F","#9DC093","#D3B2B3","#C32E30","#D4765E","#BEBEBE"};
//        int[] typeCounts = {b03,b04,b05,b06,b15,b17,b19,b20,b21,b25,b14,tother};

        for (int i = 0; i < legendList.size(); i++) {
            WarningDto data = legendList.get(i);
            typeNames[i] = data.name;
            typeColors[i] = data.color;
            typeCounts[i] = data.count;
        }

        option.color(typeColors);
        option.tooltip().show(true).formatter("{c}  {d}%");//设置显示工具格式

        Pie outPie = new Pie();//创建饼图对象
        outPie.name(title).selectedMode(SelectedMode.single).radius("50%","70%").center("50%","50%");//设置饼图的标题 半径、位置
        //填充数据
        for(int i = 0; i < typeNames.length; i++){
            Map<String,Object> map = new HashMap<>();
            map.put("value",typeCounts[i]);//填充饼图数据
            map.put("name",typeNames[i]);//填充饼图数据对应的搜索引擎
            outPie.data(map);
        }
        option.series(innerPie, outPie); //设置数据

        return option;
    }

    /**
     * 堆叠条形图
     * @param dataList
     * @return
     */
    public static GsonOption stackedBarOption(ArrayList<WarningDto> dataList) {

        int[] num1 = new int[5];int[] num2 = new int[5];int[] num3 = new int[5];int[] num4 = new int[5];int[] num5 = new int[5];int[] num6 = new int[5];
        int[] num7 = new int[5];int[] num8 = new int[5];int[] num9 = new int[5];int[] num10 = new int[5];int[] num11 = new int[5];int[] num12 = new int[5];int[] num13 = new int[5];
        for (int i = 0; i < dataList.size(); i++) {
            WarningDto dto = dataList.get(i);
            String cityWarningId = dto.item0.substring(0, 4);
            if (TextUtils.equals(cityWarningId, "2301")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num1[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num1[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num1[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num1[3]++;
                } else {
                    num1[4]++;
                }
            } else if (TextUtils.equals(cityWarningId, "2302")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num2[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num2[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num2[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num2[3]++;
                } else {
                    num2[4]++;
                }
            } else if (TextUtils.equals(cityWarningId, "2303")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num3[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num3[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num3[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num3[3]++;
                } else {
                    num3[4]++;
                }
            } else if (TextUtils.equals(cityWarningId, "2304")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num4[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num4[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num4[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num4[3]++;
                } else {
                    num4[4]++;
                }
            } else if (TextUtils.equals(cityWarningId, "2305")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num5[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num5[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num5[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num5[3]++;
                } else {
                    num5[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2306")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num6[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num6[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num6[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num6[3]++;
                } else {
                    num6[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2307")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num7[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num7[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num7[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num7[3]++;
                } else {
                    num7[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2308")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num8[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num8[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num8[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num8[3]++;
                } else {
                    num8[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2309")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num9[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num9[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num9[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num9[3]++;
                } else {
                    num9[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2310")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num10[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num10[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num10[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num10[3]++;
                } else {
                    num10[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2311")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num11[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num11[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num11[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num11[3]++;
                } else {
                    num11[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2312")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num12[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num12[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num12[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num12[3]++;
                } else {
                    num12[4]++;
                }
            }else if (TextUtils.equals(cityWarningId, "2327")) {
                if (TextUtils.equals(dto.color, "01")) {
                    num13[0]++;
                } else if (TextUtils.equals(dto.color, "02")) {
                    num13[1]++;
                } else if (TextUtils.equals(dto.color, "03")) {
                    num13[2]++;
                }else if (TextUtils.equals(dto.color, "04")) {
                    num13[3]++;
                } else {
                    num13[4]++;
                }
            }
        }

        String title = "";
        String[] levelNames = {"哈尔滨","齐齐哈尔","鸡西","鹤岗","双鸭山","大庆","伊春","佳木斯","七台河","牡丹江","黑河","绥化","大兴安岭"};
        String[] levelColors = {"#1D67C1","#F7BA34","#F98227","#D4292A","#ffffff"};
        List<int[]> levelCounts = new ArrayList<>();
        levelCounts.add(num1);levelCounts.add(num2);levelCounts.add(num3);levelCounts.add(num4);levelCounts.add(num5);levelCounts.add(num6);
        levelCounts.add(num7);levelCounts.add(num8);levelCounts.add(num9);levelCounts.add(num10);levelCounts.add(num11);levelCounts.add(num12);levelCounts.add(num13);

        GsonOption option = new GsonOption();//创建option对象
        option.color(levelColors);
        option.tooltip().trigger(Trigger.axis).axisPointer().type(PointerType.shadow);
        option.grid().left("2%").right("6%").bottom("3%").containLabel(true);

        ValueAxis valueAxis = new ValueAxis();
        valueAxis.splitLine().show(false);
        valueAxis.axisTick().show(false);
        valueAxis.axisLine().show(false);
        valueAxis.show(false);
        option.xAxis(valueAxis);

        CategoryAxis categorxAxis = new CategoryAxis();
        categorxAxis.splitLine().show(false);
        categorxAxis.axisTick().show(false);
        categorxAxis.axisLine().show(false);
        categorxAxis.boundaryGap(true);
        categorxAxis.inverse(true);
        categorxAxis.data(levelNames);
        option.yAxis(categorxAxis);

        option.title().text(title).subtext("").x("center");//设置标题  二级标题  标题位置
        option.toolbox().show(true).feature(Tool.mark);//设置工具栏 展示  能标记


        TextStyle textStyle = new TextStyle();
        textStyle.color("#000");
        ItemStyle itemStyle = new ItemStyle();
        itemStyle.normal().textStyle(textStyle).position(Position.right).show(false);

        Bar bar1 = new Bar();
        bar1.name(title).type(SeriesType.bar).stack("总量");
        Bar bar2 = new Bar();
        bar2.name(title).type(SeriesType.bar).stack("总量");
        Bar bar3 = new Bar();
        bar3.name(title).type(SeriesType.bar).stack("总量");
        Bar bar4 = new Bar();
        bar4.name(title).type(SeriesType.bar).stack("总量");
        Bar bar5 = new Bar();
        bar5.name(title).type(SeriesType.bar).stack("总量").label(itemStyle);
        for(int i = 0; i < levelNames.length; i++){
            int total = levelCounts.get(i)[0]+levelCounts.get(i)[1]+levelCounts.get(i)[2]+levelCounts.get(i)[3]+levelCounts.get(i)[4];
            Log.e("total", total+"");
            bar1.data(levelCounts.get(i)[0]);
            bar2.data(levelCounts.get(i)[1]);
            bar3.data(levelCounts.get(i)[2]);
            bar4.data(levelCounts.get(i)[3]);
            bar5.data(levelCounts.get(i)[4]);
        }
        option.series(bar1,bar2,bar3,bar4,bar5);

        return option;
    }

}
