define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({

            name: 'voc_differentiation_grid',

            multiSelect: false,

            show: {
                toolbar: true,
                toolbarSearch: true,
                toolbarReload: false,
            },
            
            toolbar: {

                items: [
                    {type: 'button', id: 'edit', caption: 'Обновить', onClick: $_DO.import_voc_differentiation, icon: 'w2ui-icon-pencil'},
                ],
            
            },

            columns: [                
                {field: 'differentiationcode', caption: 'Код', size: 9},
                {field: 'differentiationname', caption: 'Наименование', size: 25},
                {field: 'differentiationvaluekind', caption: 'Тип значений', size: 10, voc: data.vc_diff_value_types},
                {field: 'vc_nsi_list.name', caption: 'Справочник', size: 25},
                {field: 'vc_nsi_list.name', caption: 'Значения', size: 25, render: function (r) {
                    if (!r.nsiitem) return ''
                    return (r.isplural ? 'несколько' : 'одно')
                }},
                {field: 'tariffs', caption: 'Тип тарифа', size: 25},
                {field: 'nsi_268', caption: 'Вид тарифа', size: 25},
            ],
            
            records: data.records,
            
            onDblClick: null,
            
        }).refresh ()
        
        if (!data.records.length) setTimeout ($_DO.check_voc_differentiation, 10)

    }

})