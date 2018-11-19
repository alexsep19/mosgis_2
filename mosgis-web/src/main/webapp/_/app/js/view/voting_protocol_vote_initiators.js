define ([], function () {
    
    var grid_name = 'voting_protocol_vote_initiators_grid'
    
    function getData () {
        return $('body').data ('data')
    }
    
    return function (data, view) {

        var layout = w2ui ['topmost_layout']
        
        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            toolbar: {
            
                items: [
                    {type: 'button', id: 'create_owner', caption: 'Собственник', icon: 'w2ui-icon-plus', onClick: $_DO.create_owner_voting_protocol_vote_initiators},
                    {type: 'button', id: 'create_org', caption: 'Юридическое лицо', icon: 'w2ui-icon-plus', onClick: $_DO.create_org_voting_protocol_vote_initiators},
                ].filter (not_off),
                
            },

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: true,
                toolbarColumns: true,
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
                {field: 'initiator', caption: 'Инициатор', size: 10},
                {field: 'org_ogrn', caption: 'ОГРН/ОГРНИП', size: 10},
            ].filter (not_off),

            postData: {data: {"protocol_uuid": data.item.uuid, "house_uuid": data.item.house_uuid}},

            url: '/mosgis/_rest/?type=vote_initiators',
           
            onDblClick: function (e) {
                openTab ('/voting_protocol/' + e.recid)
            },
            
        })

    }
    
})