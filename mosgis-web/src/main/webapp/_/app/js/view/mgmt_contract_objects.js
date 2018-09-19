define ([], function () {
    
    var grid_name = 'mgmt_contract_objects_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var is_editable = data.item._can.edit

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_editable,
            },            

            textSearch: 'contains',
            
            columns: [              
                {field: 'fias.label', caption: 'Адрес', size: 100},
                {field: 'startdate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
                {field: '_', caption: 'Основание', size: 50, render: function (r) {
                    return r.dt ? 'ДС от ' + dt_dmy (r.dt) + ' №' + r.no : 'договор'
                }},
                {field: 'id_ctr_status',  caption: 'Статус', size: 10, voc: data.vc_gis_status},
            ],
            
            postData: {search: [
                {field: "uuid_contract", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/mosgis/_rest/?type=contract_objects',
                        
            onDblClick: function (e) {openTab ('/mgmt_contract_object/' + e.recid)},
            
            onAdd: $_DO.create_mgmt_contract_objects,
            
            onRefresh: function (e) {e.done (
            
                function (e) {

                    var grid = w2ui [e.target]

                    $('tr[recid]').each (function () {
                    
                        var $this = $(this)
                        
                        var r = grid.get ($this.attr ('recid'))
                                                
                        if (r.id_ctr_status != 10 && r.id_ctr_status_gis != r.id_ctr_status) {
                            var p = {title: 'Статус не соответствует статусу объекта ГИС ЖКХ'}
                            var $td = $('td[col=4]', $this)
                            $td.css ({background: '#fdd'}).prop (p)
                            $('div', $td).prop (p)
                        }
                                                
                    })

                }            
            
            )},            
                        
        })

    }
    
})