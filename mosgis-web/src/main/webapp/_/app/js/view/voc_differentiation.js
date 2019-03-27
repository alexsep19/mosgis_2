define ([], function () {

    return function (data, view) {
    
        var is_popup = 1 == $_SESSION.delete('voc_differentiation_popup.on')

        $((w2ui ['popup_layout'] || w2ui ['vocs_layout']).el ('main')).w2regrid ({

            name: 'voc_differentiation_grid',

            multiSelect: false,

            show: {
                toolbar: true,
                toolbarSearch: true,
                toolbarReload: false,
            },
            
            toolbar: {

                items: [
                    {type: 'button', id: 'edit', caption: 'Обновить', onClick: $_DO.import_voc_differentiation, icon: 'w2ui-icon-pencil'
                        , off: is_popup
                    }
                ].filter(not_off),
            
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
            
            onDblClick: function (e) {

                var r = this.get (e.recid)

                if (is_popup) {

                    $_SESSION.set ('voc_differentiation_popup.data', clone (r))

                    w2popup.close ()

                }
            }
        }).refresh ()
        
        if (!data.records.length) setTimeout ($_DO.check_voc_differentiation, 10)

    }

})