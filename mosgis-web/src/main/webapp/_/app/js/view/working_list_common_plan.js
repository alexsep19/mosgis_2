define ([], function () {

    return function (data, view) {

        var is_own = data.item._can.edit
        
        var columns = [
            {field: 'index_', caption: '№', size: 1},
            {field: 'vc_nsi_56', caption: 'Вид работ/услуг', size: 30},
            {field: 'w.label', caption: 'Наименование', size: 50},
            {field: 'count', caption: 'К-во в перечне', size: 10},
            {field: 'cnt', caption: 'К-во в плане', size: 10},
        ]
        
        for (var i = 0; i < 12; i ++) columns.push ({
            field: 'cnt_' + (i + 1), 
            caption: w2utils.settings.shortmonths [i].toLowerCase (), 
            size: 10,
            editable: {type: 'int', min: 1},
        })
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'working_list_common_plan_grid',
            selectType: 'cell',

            show: {
                toolbar: false,
                footer: true,
            },     
            
            multiSelect: false,           
            columns: columns,            
            records: data.records,
                                    
            onChange: $_DO.patch_working_list_common_plan,
            onContextMenu: darn,

        }).refresh ();
        
        $('#tabs_passport_layout_main_tabs_right').text ('Для ввода дат щёлкните правой кнопкой мыши на соответствующей ячейке');

    }

})