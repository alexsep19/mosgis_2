define ([], function () {
    
    var grid_name = 'mgmt_contract_objects_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var is_editable = data.item._can.edit
        var is_customer_coop = data.item ['ch.uuid']

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: is_editable,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_editable && !is_customer_coop,
            },            

            toolbar: {
            
                items: !is_editable || !is_customer_coop ? [] : [
                    {type: 'button', id: 'add', caption: 'Добавить из устава', onClick: $_DO.add_mgmt_contract_objects, icon: 'w2ui-icon-plus'},
                ].filter (not_off),
                
            },             

            textSearch: 'contains',
            
            columns: [              
                {field: 'fias.label', caption: 'Адрес', size: 100},
                {field: 'startdate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
                {field: '_', caption: 'Основание', size: 50, render: function (r) {
                    return r.dt ? 'ДС от ' + dt_dmy (r.dt) + ' №' + r.no : 'договор'
                }},
                {field: 'id_ctr_status_gis',  caption: 'Статус', size: 10, voc: data.vc_gis_status},
            ],
            
            postData: {search: [
                {field: "uuid_contract", operator: "is", value: $_REQUEST.id},
            ]},

            url: '/_back/?type=contract_objects',
            
            limit: 100000,
                        
            onDblClick: function (e) {openTab ('/mgmt_contract_object/' + e.recid)},
            
            onAdd: $_DO.create_mgmt_contract_objects,
            
            onRefresh: data.item.id_customer_type != 1 ? null : function (e) {e.done (

                function (e) {

                    var grid = w2ui [e.target]

                    $('tr[recid]').each (function () {
                    
                        var $this = $(this)
                    
                        var recid = $this.attr ('recid')
                        
                        var r = grid.get (recid)
                        
                        var last_approve_ts = data.item.last_approve ? data.item.last_approve.ts : '9999'
                        
                        if (r ['log.ts'] > last_approve_ts) {
                        
                            $('td', $this).css ({background: '#ffc'})
                        
                        }
                        
                        if (!r.is_annuled) grid.toolbar.hide ('w2ui-add')
                        
                    })

                }            

            )},            
                        
        })

    }
    
})