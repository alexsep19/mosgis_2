define ([], function () {
    
    var grid_name = 'mgmt_contract_agreements_grid'
    
    function getData () {
        return $('body').data ('data')
    }
            
    return function (data, view) {
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))
        
        var is_own = (data.item.uuid_org = $_USER.uuid_org)

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: 1,
                toolbarReload: false,
                toolbarColumns: false,
                toolbarInput: false,
                toolbarAdd: is_own,
                toolbarDelete: is_own,
                toolbarEdit: is_own,
            },            

            textSearch: 'contains',

            columns: [              
                {field: 'agreementnumber', caption: 'Номер', size: 20},
                {field: 'agreementdate', caption: 'Дата', size: 18, render: _dt},
                {field: 'label', caption: 'Наименование', size: 100},
                {field: 'len', caption: 'Объём, Мб', size: 10, render: function (r) {return (r.len/1024/1024).toFixed(3)}},
                {field: 'description', caption: 'Описание', size: 50},
            ],
            
            postData: {search: [
                {field: "uuid_contract", operator: "is", value: $_REQUEST.id},
                {field: "id_type",       operator: "is", value: 1}
            ]},

            url: '/mosgis/_rest/?type=contract_docs',
            
            onDblClick: $_DO.download_mgmt_contract_agreements,
            
            onDelete: $_DO.delete_mgmt_contract_agreements,
            
            onAdd: $_DO.create_mgmt_contract_agreements,
            
            onEdit: $_DO.edit_mgmt_contract_agreements,
                        
//            onChange: $_DO.patch_mgmt_contract_common,
            
        })

    }
    
})