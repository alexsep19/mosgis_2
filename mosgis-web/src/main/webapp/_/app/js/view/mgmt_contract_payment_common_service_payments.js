define ([], function () {

    return function (data, view) {
    
        var is_own = data.item._can.edit
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'mgmt_contract_payment_common_service_payments_grid',

            show: {
                toolbar: is_own,
                toolbarReload: false,
                toolbarAdd: true,
                toolbarDelete: true,
                toolbarInput: false,
                footer: true,
            },     
            
            multiSelect: false,
            
            columns: [              
                {field: 'label', caption: 'Услуга', size: 50},
                {field: 'servicepaymentsize', caption: 'Размер платы', size: 100, editable: !is_own ? null : {type: 'float:4', min: 0}},
            ],
            
            records: data.records,
            
            onAdd: function () {
                use.block ('mgmt_contract_payment_common_service_payments_popup')
            },
            
            onDelete: $_DO.delete_mgmt_contract_payment_common_service_payments,
            
            onChange: $_DO.patch_mgmt_contract_payment_common_service_payments,

        }).refresh ();

    }

})