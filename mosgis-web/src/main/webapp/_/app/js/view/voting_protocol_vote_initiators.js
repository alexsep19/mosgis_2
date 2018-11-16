define ([], function () {
    
    var grid_name = 'voting_protocol_vote_initiators_grid'
    
    function getData () {
        return $('body').data ('data')
    }
    
    return function (data, view) {

        var layout = w2ui ['topmost_layout']
        
        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: true,
                toolbarColumns: true,
                toolbarAdd: true,
            },            

            textSearch: 'contains',

            searches: [            
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),

            columns: 
            [                
                {field: 'ind_label', caption: 'ФИО собственника', size: 10},
                {field: 'org_label', caption: 'Наименование организации', size: 10},
                {field: 'org_ogrn', caption: 'ОГРН/ОГРНИП', size: 10},
            ].filter (not_off),

            //postData: {data: {"uuid_house": data.item.fiashouseguid}},

            url: '/mosgis/_rest/?type=vote_initiators',
           
            onDblClick: function (e) {
                openTab ('/voting_protocol/' + e.recid)
            },

            onAdd: $_DO.create_voting_protocol_vote_initiators
            
        })

    }
    
})