define ([], function () {

    function colorize () {               
        $('tr[recid] td[data-editable]').each (function () {
            var $this = $(this)
            $this.css ({background: '#ffffcc'})
        })                
    }

    return function (data, view) {
    
        var it = data.item

        var is_own = it._can.edit_plan
        
        var columns = [
            {field: 'index_', caption: '№', size: 1},
            {field: 'vc_nsi_56', caption: 'Вид работ/услуг', size: 30},
            {field: 'w.label', caption: 'Наименование', size: 50},
            {field: 'count', caption: 'К-во в перечне', size: 10},
            {field: 'cnt', caption: 'К-во в плане', size: 10},
        ]

        for (var i = 0; i < 12; i ++) {
        
            var col = {
                field: 'cnt_' + (i + 1), 
                caption: w2utils.settings.shortmonths [i].toLowerCase (), 
                size: 10
            }
            
            if (is_own) {
            
                var m = i + 1
            
                var dt = it.year + '-'
                if (m < 10) dt += '0'
                dt += m
                dt += '-01'

                if (it ['tb_work_lists.dt_from'].substr (0, 10) <= dt && dt <= it ['tb_work_lists.dt_to']) {
                    col.editable = {type: 'int', min: 1}
                    col.attr = 'data-editable'
                }

            }
        
            columns.push (col)
        
        }
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'working_plan_common_plan_grid',
            selectType: 'cell',

            show: {
                toolbar: false,
                footer: true,
            },     
            
            multiSelect: false,           
            columns: columns,            
            records: data.records,
            reorderColumns: false,
                                    
            onChange: $_DO.patch_working_plan_common_plan,
            
            onUnselect: function (e) {
                e.done (colorize)
            },

            onRefresh: function (e) {
                e.done (colorize)
            },
            
            onContextMenu: !is_own ? null : function (e) {
                if (!this.columns [e.column].editable) return
                $_SESSION.set ('cell', {uuid: it.uuid, uuid_working_list_item: e.recid, year: it.year, month: e.column - 5})
                use.block ('working_plan_common_plan_dates_popup')
            },

        }).refresh ();
        
        if (is_own) $('#tabs_passport_layout_main_tabs_right').text ('Для ввода дат щёлкните правой кнопкой мыши на соответствующей ячейке');

    }

})