define ([], function () {

    var b = ['delete', 'undelete']

    function recalcToolbar (e) {e.done (function () {

        var g = w2ui ['mgmt_contracts_grid']

        var t = g.toolbar

        t.disable (b [0])
//        t.disable (b [1])
        
        if (g.getSelection ().length != 1) return

        t.enable (b [g.get (g.getSelection () [0]).is_deleted])

    })}
    
    var postData = {}
    if ($_USER.role.nsi_20_1)          postData.uuid_org          = $_USER.uuid_org
    if ($_USER.is_building_society ()) postData.uuid_org_customer = $_USER.uuid_org
    
    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['rosters_layout'].el ('main')).w2regrid ({ 

            name: 'mgmt_contracts_grid',

            show: {
                toolbar: true,
                toolbarAdd: $_USER.role.nsi_20_1,
//                toolbarEdit: $_USER.role.nsi_20_1,
                footer: true,
            },     
            
            toolbar: {
            
                items: [
                
                    {
                        type: 'button', 
                        id: 'import_objects', 
                        caption: 'Импорт УО...', 
                        icon: 'w2ui-icon-plus', 
                        onClick: $_DO.import_objects_mgmt_contracts, 
                        off: !$_USER.role.nsi_20_1
                    },
                    
                ].filter (not_off),
                
            },            

            searches: [            
                
                {field: 'docnum', caption: 'Номер',  type: 'text'},
                {field: 'signingdate', caption: 'Дата заключения',  type: 'date'},
                {field: 'id_ctr_status', caption: 'Статус договора', type: 'enum', options: {items: data.vc_gis_status.items.filter (function (i) {
                    switch (i.id) {
                        case 50:
                        case 60:
                        case 80:
                            return false;
                        default:
                            return true;
                    }
                })}}, 
                {field: 'uuid_org', caption: 'Исполнитель', type: 'enum', options: {items: data.vc_orgs.items}, off: $_USER.role.nsi_20_1},
                {field: 'id_customer_type', caption: 'Тип заказчика', type: 'enum', options: {items: data.vc_gis_customer_type.items}, off: !($_USER.role.admin || $_USER.role.nsi_20_1)},
                {field: 'uuid_org_customer', caption: 'Заказчик', type: 'enum', options: {items: data.customers.items}, off: !($_USER.role.admin || $_USER.role.nsi_20_1)},
                {field: 'contractbase', caption: 'Основание заключения', type: 'enum', options: {items: data.vc_nsi_58.items}},
                {field: 'effectivedate', caption: 'Дата вступления в силу',  type: 'date'},
                {field: 'plandatecomptetion', caption: 'Дата окончания',  type: 'date'},
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),

            columns: [                
            
                {field: 'docnum', caption: 'Номер', size: 20},
                {field: 'signingdate', caption: 'Дата заключения', size: 18, render: _dt},
                {field: 'id_ctr_status',  caption: 'Статус',     size: 10, voc: data.vc_gis_status},
                {field: 'org.label', caption: 'Исполнитель', size: 100, off: $_USER.role.nsi_20_1},               
                {field: 'id_customer_type',  caption: 'Вид заказчика',     size: 30, voc: data.vc_gis_customer_type},                
                {field: 'org_customer.label', caption: 'Заказчик', size: 100},
                {field: 'contractbase',  caption: 'Основание заключения',     size: 30, voc: data.vc_nsi_58},                
                {field: 'effectivedate', caption: 'Дата вступления в силу', size: 18, render: _dt},
                {field: 'plandatecomptetion', caption: 'Дата окончания', size: 18, render: _dt},               
                
            ].filter (not_off),
            
            postData: {data: postData},

            url: '/mosgis/_rest/?type=mgmt_contracts',
                        
            onAdd:      $_DO.create_mgmt_contracts,            
            
            onDblClick: function (e) {
                openTab ('/mgmt_contract/' + e.recid)
            },
            
            onRefresh: function (e) {e.done (
            
                function (e) {

                    var grid = w2ui [e.target]

                    $('tr[recid]').each (function () {
                    
                        var $this = $(this)
                        
                        var r = grid.get ($this.attr ('recid'))
                                                
                        if (r ['out_soap.err_text']) {
                            var p = {title: 'Ошибка передачи в ГИС ЖХХ'}
                            var $td = $('td[col=0]', $this)
                            $td.css ({background: '#fdd'}).prop (p)
                            $('div', $td).prop (p)
                        }

                        if (r.id_ctr_status != 10 && r.id_ctr_status_gis != r.id_ctr_status) {
                            var p = {title: 'Статус не соответствует статусу объекта ГИС ЖКХ'}
                            var $td = $('td[col=2]', $this)
                            $td.css ({background: '#fdd'}).prop (p)
                            $('div', $td).prop (p)
                        }
                                                
                    })

                }            
            
            )},
            
            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,

        }).refresh ();

    }

})