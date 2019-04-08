define ([], function () {
    
    var grid_name = 'supply_resource_contract_accounts_grid'
                
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: 1,
                footer: 1,
                toolbarReload: true,
                toolbarColumns: false,
                toolbarInput: true,
            },            
            
            toolbar: {
            
                items: [
                    {type: 'button', id: 'ind', caption: 'Добавить ЛС физического лица', onClick: $_DO.create_supply_resource_contract_accounts, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                    {type: 'button', id: 'org', caption: 'Добавить ЛС юридического лица', onClick: $_DO.create_supply_resource_contract_accounts, off: !data.item._can.create_account, icon: 'w2ui-icon-plus'},
                ].filter (not_off),
                
            },
            
            textSearch: 'contains',
            
            columnGroups : [
                {master: true},
                {span: 3, caption: 'Площадь, м\xB2'},
                {master: true},
                {span: 4, caption: 'Плательщик'},
                {master: true},
            ],            
            
            columns: [              
                {field: 'accountnumber', caption: 'Номер', size: 20},
                
                {field: 'totalsquare', caption: 'Общая', size: 20, render: 'float:2'},
                {field: 'residentialsquare', caption: 'Жилая', size: 20, render: 'float:2'},
                {field: 'heatedarea', caption: 'Отапливаемая', size: 20, render: 'float:2'},

                {field: 'livingpersonsnumber', caption: 'К-во прож.', size: 20, render: 'int'},
                
                {field: 'ind.label', caption: 'Физ. лицо', size: 50},
                {field: 'org.label', caption: 'Юр. лицо', size: 50},
                {field: 'isrenter',  caption: 'Нанинматель?', size: 10, voc: {0: 'нет', 1: 'да'}},
                {field: 'isaccountsdivided', caption: 'Разделён?', size: 10, voc: {0: 'нет', 1: 'да'}},
                {field: 'id_ctr_status', caption: 'Статус', size: 10, voc: data.vc_gis_status},
            ],
            
            postData: {data: {uuid_sr_contract: $_REQUEST.id}},

            url: '/_back/?type=accounts',
                                    
            onDblClick: function (e) {openTab ('/account/' + e.recid)},
            
            onAdd: $_DO.create_supply_resource_contract_accounts,
            
        })

    }
    
})