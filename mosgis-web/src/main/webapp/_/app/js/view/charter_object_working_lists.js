define ([], function () {
    
    var grid_name = 'working_lists_grid'
    
    function _my (x, y, z, t) {
        var p = t.split ('-')
        return w2utils.settings.fullmonths [parseInt (p [1]) - 1] + ' ' + p [0]
    }

    return function (data, view) {

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        var is_editable = data.item._can.edit_work_list

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarInput: false,
                toolbarAdd: is_editable,
            },            

            textSearch: 'contains',

            searches: [            
            
                {field: "uuid_charter_object", operator: "is", type: 'text', value: $_REQUEST.id, hidden: true},

                {field: 'dt', caption: 'Период', type: 'list', options: {items: data.periods}},
            
                {field: 'id_ctr_status', caption: 'Статус', type: 'enum', options: {items: data.vc_gis_status.items.filter (function (i) {
                    switch (i.id) {
                        case 50:
                        case 60:
                        case 80:
                            return false;
                        default:
                            return true;
                    }
                })}}, 

            ].filter (not_off),
            
            columns: [                               
                {field: 'dt_from', caption: 'Начало', size: 20, render: _my},
                {field: 'dt_to', caption: 'Окончание', size: 20, render: _my},
                {field: 'count', caption: 'Количество', size: 10},
                {field: 'totalcost', caption: 'Общая стоимость', size: 20},
                {field: 'id_ctr_status', caption: 'Статус', size: 100, voc: data.vc_gis_status},
            ],
            
            url: '/_back/?type=working_lists',
                                    
            onAdd: $_DO.create_charter_object_working_lists,
            
            onDblClick: function (e) {
                openTab ('/working_list/' + e.recid)
            },
                        
        })

    }
    
})