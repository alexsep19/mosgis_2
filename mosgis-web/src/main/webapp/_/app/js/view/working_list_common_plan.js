define ([], function () {

    return function (data, view) {

        var is_own = data.item._can.edit_plan
        
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
            editable: !is_own ? false : {type: 'int', min: 1},
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
            reorderColumns: false,
                                    
            onChange: $_DO.patch_working_list_common_plan,
            
            onContextMenu: !is_own ? null : function (e) {
                $_SESSION.set ('cell', {uuid: data.plan.uuid, uuid_working_list_item: e.recid, year: data.plan.year, month: e.column - 5})
                use.block ('working_list_common_plan_dates_popup')
            },

        }).refresh ();
        
        if (is_own) $('#tabs_passport_layout_main_tabs_right').text ('Для ввода дат щёлкните правой кнопкой мыши на соответствующей ячейке');

    }

})